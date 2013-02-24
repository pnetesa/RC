package com.rc;

import static com.rc.base.Output.print;

import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.rc.base.CommandFunc;
import com.rc.base.Executor;
import com.rc.base.ParamFunc;
import com.rc.ui.VideoDetectorActivity;
import com.rc.util.Consts;
import com.rc.util.IntentFor;
import com.rc.util.UsbConnector;

public class MainExecutor extends Executor {

	private static MainExecutor mInstance;

	private UsbConnector mUsbConnector;
	private byte[] mWriteArgs;
	private int mIntervalRead = 0;


	private Handler mHandler = new Handler();
	private Timer mTimer;
	private DateFormat mDateFormat = 
			DateFormat.getTimeInstance(DateFormat.MEDIUM);

	public static MainExecutor getInstance(Context context) {

		if (mInstance == null)
			mInstance = new MainExecutor(context);

		return mInstance;
	}

	private MainExecutor(Context context) {
		super(context);
		mUsbConnector = new UsbConnector(context);

		registerCommand("c", new CommandFunc() {

			@Override
			public void execute() {
				MainExecutor.this.connect();
			}
		});

		registerCommand("d", new CommandFunc() {

			@Override
			public void execute() {
				MainExecutor.this.disconnect();
			}
		});

		registerCommand("r", new CommandFunc() {

			@Override
			public void execute() {
				MainExecutor.this.read();
			}
		});

		registerParam("ir", new ParamFunc() {

			@Override
			public void execute(String value) {
				MainExecutor.this.setIntervalRead(value);
			}
		});

		registerCommand("ir", new CommandFunc() {

			@Override
			public void execute() {
				MainExecutor.this.getIntervalRead();
			}
		});

		registerCommand("ht", new CommandFunc() {

			@Override
			public void execute() {
				MainExecutor.this.halt();
			}
		});

		registerCommand("38", new CommandFunc() {

			@Override
			public void execute() {
				MainExecutor.this.pressUp();
			}
		});

		registerParam("s", new ParamFunc() {

			@Override
			public void execute(String value) {
				MainExecutor.this.setS(value);
			}
		});

		registerCommand("s", new CommandFunc() {

			@Override
			public void execute() {
				MainExecutor.this.getS();
			}
		});
		
		registerCommand("rd", new CommandFunc() {
			
			@Override
			public void execute() {
				MainExecutor.this.runVideoDetector();
			}
		});
		
		registerCommand("sd", new CommandFunc() {
			
			@Override
			public void execute() {
				MainExecutor.this.stopVideoDetector();
			}
		});
		
		registerParam("md", new ParamFunc() {

			@Override
			public void execute(String value) {
				MainExecutor.this.markerDetected(value);
			}
		});
	}

	private void connect() {
		mUsbConnector.connect();
		print("usb connection opened");
	}

	private void disconnect() {
		mUsbConnector.disconnect();
		print("usb connection closed");
	}

	private void read() {
		int bytesWritten = mUsbConnector.write(mWriteArgs);
		byte[] bytes = mUsbConnector.read(1024);

		print("bytes written: " + bytesWritten);
		print(formatNumbers(bytes));
	}

	private void halt() {
		
		setIntervalRead(String.valueOf(0)); // for example...
		print("halt!");
	}

	private void pressUp() {
		print("You pressed UP key!");
	}

	private void setS(String values) {
		
		byte[] writeArgs = toBytes(values);
		mWriteArgs = writeArgs;
		print("set 's' to " + formatNumbers(writeArgs));
	}

	private void getS() {
		if (mWriteArgs == null) {
			print("undefined");
			return;
		}
		
		print("s = " + formatNumbers(mWriteArgs));
	}

	private void setIntervalRead(String value) {
		
		try {
			mIntervalRead = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterException(
					"'interval read' must be numeric int value");
		}
		
		if (mTimer != null)
			mTimer.cancel();

		if (mIntervalRead == 0) {
			print("interval read off");
			return;
		}
		
		long period = TimeUnit.SECONDS.toMillis(mIntervalRead);
		
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				MainExecutor.this.intervalReadSafe();
			}
			
		}, period, period);
		
		print("interval read: " + mIntervalRead + " second(s)");
	}

	private void intervalReadSafe() {
		
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				try {
					
					// Add timestamp
					print();
					print(mDateFormat.format(
							new Date(System.currentTimeMillis())));
					
					MainExecutor.this.read();
				} catch (Exception e) {
					String msg = e.getCause() == null ? e.getMessage()
							: e.getCause().getMessage();
					print("error: " + msg);
				}
			}
		});
		
	}

	private void getIntervalRead() {
		print("ir = " + mIntervalRead + " second(s)");
	}

	private void markerDetected(String value) {
		
		String[] pair = value.split(",");
		int offsetX = Integer.parseInt(pair[0]);
		int offsetY = Integer.parseInt(pair[1]);
		
		print(Consts.NEW_LINE + "marker detected " 
				+ mDateFormat.format(new Date(System.currentTimeMillis())));
		print(String.format("offset: x=%d, y=%d", offsetX, offsetY));
		
		final int CENTER_AREA = 30; // For 60 x 60 square
		if (Math.abs(offsetX) <= CENTER_AREA &&
				Math.abs(offsetY) <= CENTER_AREA) {
			
			print("landing...");
			
		} else {
			
			// Center...
			
		}
	}

	private void runVideoDetector() {
		
		if (VideoDetectorActivity.isRunning) {
			print("video detector is up and running");
			return;
		}
		
		print("starting video detector...");
		
		Intent intent = IntentFor.videoDetectorActivity(mContext);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}

	private void stopVideoDetector() {
		print("stopping video detector...");
		mContext.sendBroadcast(IntentFor.stopDetectorAction());
	}
}
