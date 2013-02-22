package com.rc.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.rc.MainExecutor;
import com.rc.base.Executor;
import com.rc.util.Consts;
import com.rc.util.IntentFor;
import com.rc.util.Preferences;

public class VideoDetectorActivity extends Activity implements CvCameraViewListener {
	
	public static final String TAG = VideoDetectorActivity.class
			.getSimpleName();
	
	private int mSceneCenterX;
	private int mSceneCenterY;
	
    private static final Scalar INFO_COLOR = new Scalar(0, 255, 0, 255);
	private Point mTextPoint = new Point(0, 25);
	private Point mTextPoint2 = new Point(0, 55);
	private Point mTextPoint3 = new Point(0, 85);
	
    private Point mLftTop = new Point(0, 0);
    private Point mRhtBtm = new Point(0, 0); 
    
	private JavaCameraView mCameraView;
	
	private Mat mRgba;
	private Mat mGray;
	
	private Mat mMarker;
	private Mat mMarkerSkewed;
	
	private MatOfKeyPoint mMarkerKeyPoints;
	private Mat mMarkerDescriptors;
	
	private MatOfKeyPoint mSceneKeyPoints;
	private Mat mSceneDescriptors;
	
	private MatOfDMatch mMatchesMat;
	
	private List<Point> mMarkerPoints = new ArrayList<Point>();
	private List<Point> mScenePoints = new ArrayList<Point>();
	private Mat m32FC2Mat;
	private MatOfPoint2f mMarkerPointsMat;
	private MatOfPoint2f mScenePointsMat;
	
	private MatOfPoint2f mMarkerCornersMat;
	private MatOfPoint2f mSceneCornersMat;
	
	private FeatureDetector mDetector;
	private DescriptorExtractor mExtractor;
	private DescriptorMatcher mMatcher;
	
	private int mGoodMatchesCount;
	
	private long mCounter;
	private long mSkipCount;
	private double mDistance;
	
	private short mDetectTracker = 1;
	private int mDetectRate;
	
	private Executor mMainExec;
	
	public static boolean isRunning;
	
	
	private Preferences mPrefs;
	
	private HashMap<String, DetectTypes> mNameToTypes = new HashMap<String, DetectTypes>();
	{
		mNameToTypes.put("s", new DetectTypes(FeatureDetector.SURF,
				DescriptorExtractor.SURF, DescriptorMatcher.FLANNBASED));
		mNameToTypes.put("o", new DetectTypes(FeatureDetector.ORB,
				DescriptorExtractor.ORB, DescriptorMatcher.BRUTEFORCE_HAMMING));
		mNameToTypes.put("b",
				new DetectTypes(FeatureDetector.BRISK, DescriptorExtractor.BRISK,
						DescriptorMatcher.BRUTEFORCE_HAMMING));
		mNameToTypes.put("f", new DetectTypes(FeatureDetector.FAST,
				DescriptorExtractor.BRIEF, DescriptorMatcher.BRUTEFORCE));
	}
	
	class DetectTypes {
		
		public final int detectorType;
		public final int extractorType;
		public final int matcherType;
		
		public DetectTypes(int detectorType, int extractorType, int matcherType) {
			this.detectorType = detectorType;
			this.extractorType = extractorType;
			this.matcherType = matcherType;
		}
	}
	
	private IntentFilter mStopFilter = new IntentFilter();
	private BroadcastReceiver mStopBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			VideoDetectorActivity.this.finish();
		}
	};
	
	private LoaderCallbackInterface mLoaderCallback = new BaseLoaderCallback(this) {
		
		@Override
		public void onManagerConnected(int status) {
			
			if (status == LoaderCallbackInterface.SUCCESS) {
				
				DetectTypes types = mNameToTypes.get(mPrefs.detectMethod());
				mDetector = FeatureDetector.create(types.detectorType);
				mExtractor = DescriptorExtractor.create(types.extractorType);
				mMatcher = DescriptorMatcher.create(types.matcherType);
				
				mCameraView.enableView();
			} else {
				super.onManagerConnected(status);
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_video_detector);
		
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | 
        	    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
        	    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
        	    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
        	    WindowManager.LayoutParams.FLAG_FULLSCREEN | 
        	    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
        	    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
        	    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		mMainExec = MainExecutor.getInstance(getApplicationContext());
		mPrefs = Preferences.getInstance(getApplicationContext());
		mSkipCount = mPrefs.skipCount();
		mDetectRate = mPrefs.detectRate();
		
		int sceneWidth = "l".equals(mPrefs.frameSize()) ? 800 : 
				"m".equals(mPrefs.frameSize()) ? 640 : 320;
		int sceneHeight = "l".equals(mPrefs.frameSize()) ? 600 : 
				"m".equals(mPrefs.frameSize()) ? 480 : 240;
		mSceneCenterX = sceneWidth / 2;
		mSceneCenterY = sceneHeight / 2;
		
		mCameraView = (JavaCameraView) findViewById(R.id.camera_view);
		mCameraView.setCvCameraViewListener(this);
		mCameraView.setMaxFrameSize(sceneWidth, sceneHeight);
		
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mStopFilter.addAction(Consts.STOP_VIDEO_DETECTOR_ACTION);
	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
		
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		registerReceiver(mStopBroadcastReceiver, mStopFilter);
		isRunning = true;
	}
	
	@Override
	protected void onPause() {
		
		if (mCameraView != null)
			mCameraView.disableView();
		
		unregisterReceiver(mStopBroadcastReceiver);
		
		super.onPause();
		
		isRunning = false;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCameraView.disableView();
	}

	@Override
	public Mat onCameraFrame(Mat sceneMat) {
		
        sceneMat.copyTo(mRgba);
        Imgproc.cvtColor(sceneMat, mGray, Imgproc.COLOR_RGBA2GRAY);
        
        if (mCounter++ % mSkipCount == 0) { // Skip frames
        	
			mDistance = detectSample(mMarker);
			
			if (!isFound()) {
				mDistance = detectSample(mMarkerSkewed);
			}
        }
        
//        String text = "diag: " + (Double.isInfinite(mDistance) ? "~" : (int)mDistance);
        String text = "diag: " + (Double.isInfinite(mDistance) ? "~" : (int)mDistance) + " gm: " + mGoodMatchesCount;
        Core.putText(mRgba, text, mTextPoint, 2, 1, INFO_COLOR);
        
        if (isDetected()) {
            Core.rectangle(mRgba, mLftTop, mRhtBtm, INFO_COLOR, 3);
            executeDetected();
        }
		
		return mRgba;
	}
	
	private void executeDetected() {
		
		// Marker center
		int oX = (int) ((mRhtBtm.x - mLftTop.x) / 2 + mLftTop.x);
		int oY = (int) ((mRhtBtm.y - mLftTop.y) / 2 + mLftTop.y);
		
		int x = mSceneCenterX - oX;
		int y = mSceneCenterY - oY;
		
        String text = String.format("x=%d, y=%d", x, y);
        Core.putText(mRgba, "marker detected!", mTextPoint2, 2, 1, INFO_COLOR);
        Core.putText(mRgba, text, mTextPoint3, 2, 1, INFO_COLOR);
		
		String commandText = String.format("md %d,%d", x, y);
		mMainExec.execute(commandText);
	}
	
	private boolean isFound() {
		return !Double.isInfinite(mDistance) && mDistance > 1;
	}
	
	private void trackFound(boolean isFound) {
		mDetectTracker <<= 1;
		mDetectTracker |= isFound ? 1 : 0;
		
		if (mDetectTracker == 0)
			mDetectTracker = 1;
	}
	
	private boolean isDetected() {
		
		int detectedCount = -1;
		int count = 0;
		short bit = 1;
		for (int i = 0; i < 16; i++) {
			
			if ((bit & mDetectTracker) == bit)
				detectedCount++;
			
			bit <<= 1;
		}
		
		return detectedCount >= mDetectRate;
	}

	private double detectSample(Mat sampleMat) {
		
		// Find key points
		mDetector.detect(sampleMat, mMarkerKeyPoints);
		mDetector.detect(mGray, mSceneKeyPoints);
		
		// Calculate descriptors
		mExtractor.compute(sampleMat, mMarkerKeyPoints, mMarkerDescriptors);
		mExtractor.compute(mGray, mSceneKeyPoints, mSceneDescriptors);
		
		// Match descriptors' vectors
		mMatcher.match(mMarkerDescriptors, mSceneDescriptors, mMatchesMat);
		
		// Find out absolute MIN distance between all sample descriptors
		float minDistance = Float.MAX_VALUE;
		DMatch[] matches = mMatchesMat.toArray();
		for (DMatch match : matches) {
			minDistance = Math.min(minDistance, match.distance);
		}
		
		mGoodMatchesCount = 0;
		mMarkerPoints.clear();
		mScenePoints.clear();
		
		KeyPoint[] markerKeyPoints = mMarkerKeyPoints.toArray();
		KeyPoint[] sceneKeyPoints = mSceneKeyPoints.toArray();
		
		float distanceLimit = minDistance * 3;
		for (DMatch match : matches) {
			
			if (match.distance < distanceLimit) {
				
				mGoodMatchesCount++;
				mMarkerPoints.add(markerKeyPoints[match.queryIdx].pt);
				mScenePoints.add(sceneKeyPoints[match.trainIdx].pt);
			}
		}
		
		if (mMarkerPoints.isEmpty())
			return 0;
		
		mMarkerPointsMat.fromList(mMarkerPoints);
		mScenePointsMat.fromList(mScenePoints);
		Mat homography = Calib3d.findHomography(
				mMarkerPointsMat, mScenePointsMat, Calib3d.RANSAC, 10); // TODO: calibrate last param
		
		Core.perspectiveTransform(mMarkerCornersMat, mSceneCornersMat, homography);
		homography.release();
		
		Point[] corners = mSceneCornersMat.toArray(); 
		mLftTop = corners[0];
		mRhtBtm = corners[corners.length - 1];
		
		// TODO: remove!
		return Math.sqrt(
				Math.pow(mLftTop.x - mRhtBtm.x, 2) + 
				Math.pow(mLftTop.y - mRhtBtm.y, 2));
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat();
		mGray = new Mat();
		
		// Load sample images
		mMarker = createSample(R.raw.finish);
		mMarkerSkewed = createSample(R.raw.finish_skewed);
		
		mMarkerKeyPoints = new MatOfKeyPoint();
		mMarkerDescriptors = new Mat();
		
		mSceneKeyPoints = new MatOfKeyPoint();
		mSceneDescriptors = new Mat();
		
		mMatchesMat = new MatOfDMatch();
		m32FC2Mat = new Mat(1, 1, CvType.CV_32FC2);
		mMarkerPointsMat = new MatOfPoint2f(m32FC2Mat);
		mScenePointsMat = new MatOfPoint2f(m32FC2Mat);
		
		mMarkerCornersMat = new MatOfPoint2f();
		mSceneCornersMat = new MatOfPoint2f();
		
		int y = mMarker.rows();
		int x = mMarker.cols();
		
		mMarkerCornersMat.fromArray(
				new Point(0, 0), new Point(x, 0),
				new Point(x, y), new Point(0, y));
		mSceneCornersMat.fromArray(
				new Point(0, 0), new Point(x, 0), 
				new Point(x, y), new Point(0, y));
	}

	private Mat createSample(int resId) {
		InputStream input = getResources().openRawResource(resId); 
		Bitmap bitmap = BitmapFactory.decodeStream(input);
		
		Mat mat = new Mat();
		Utils.bitmapToMat(bitmap, mat);
		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY);
		
		return mat;
	}

	@Override
	public void onCameraViewStopped() {
		mRgba.release();
		mGray.release();
		
		mMarker.release();
		mMarkerSkewed.release();
		
		mMarkerKeyPoints.release();
		mMarkerDescriptors.release();
		
		mSceneKeyPoints.release();
		mSceneDescriptors.release();
		
		mMatchesMat.release();
		m32FC2Mat.release();
		mMarkerPointsMat.release();
		mScenePointsMat.release();
		
		mMarkerCornersMat.release();
		mSceneCornersMat.release();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_video_detector, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId() == R.id.menu_configurator) {
			startActivity(IntentFor.mainActivity(this));
		}
		
		return true;
	}
}
