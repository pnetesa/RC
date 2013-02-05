package com.rc.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import static com.rc.base.Output.*;
import com.rc.ConfigExecutor;
import com.rc.MainExecutor;
import com.rc.base.Executor;
import com.rc.base.Output;
import com.rc.base.OutputFunc;
import com.rc.util.IntentFor;
import com.rc.util.Keys;

public class MainActivity extends Activity {
	
	public static final String TAG = MainActivity.class.getSimpleName();
	
	private RelativeLayout mLayout;
	private EditText mInput;
	private TextView mOutput;
	private ScrollView mScroll;
	
	private boolean mResizeRequired;
	
	private Executor mConfigExec;
	private Executor mMainExec;
	
	private RemoteControlService mService;
	private boolean mBound;
	
	private int mLinesCount;
	
	private OutputFunc mOutputCallback = new OutputFunc() {
		
		@Override
		public void write(final String text) {
			mOutput.post(new Runnable() {
				
				@Override
				public void run() {
					writeOutput(text);
				}
			});
		}
	};
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			RemoteControlService.ServiceBinder binder = 
					(RemoteControlService.ServiceBinder) service;
			mService = binder.getService();
			mBound = true;
			
			print("remote control is running");
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			mBound = false;
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    	
    	mLayout = (RelativeLayout) findViewById(R.id.layout);
    	mInput = (EditText) findViewById(R.id.input);
        mOutput = (TextView) findViewById(R.id.output);
        mScroll = (ScrollView) findViewById(R.id.scroll);
        
        mInput.setOnKeyListener(onInputKey());
        mLayout.setOnCreateContextMenuListener(this);
        mOutput.setOnTouchListener(onOutputTouch());
        
		mMainExec = MainExecutor.getInstance(getApplicationContext());
        mConfigExec = new ConfigExecutor(this);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

	@Override
	protected void onStart() {
		super.onStart();
		
		if (RemoteControlService.isRunning)
			bindService(IntentFor.commandsService(this), 
					mConnection, BIND_AUTO_CREATE);
		
		Output.registerOutput(mOutputCallback);
	}
    
    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
    	super.onRestoreInstanceState(savedState);
    	
		CharSequence outputText = 
				savedState.getCharSequence(Keys.OUTPUT_TEXT);
		mOutput.setText(outputText);
    }
	
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	mResizeRequired = false;
    	
		scrollToEnd();
    	setInputFocus();
	}
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// Output text state
    	outState.putCharSequence(Keys.OUTPUT_TEXT, mOutput.getText());
    	
    	super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
	@Override
	protected void onStop() {
		super.onStart();
		
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		Output.unregisterOutput(mOutputCallback);
	}
	
	private OnKeyListener onInputKey() {
		return new OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					String text = mInput.getText().toString();
					mInput.setText("");
					
		    		executeCommand(text);
		    		resizeControls();
		    		scrollToEnd();
		    		setInputFocus();
					return true;
				}
				return false;
			}
		};
	}

	private OnTouchListener onOutputTouch() {
		return new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
    			mOutput.setOnTouchListener(null);
    			resizeControls();
	    		scrollToEnd();
    	    	setInputFocus();
    			return true;
			}
		};
	}

	private void executeCommand(String commandText) {
		commandText = commandText.trim();
		if (TextUtils.isEmpty(commandText))
			return;
		
		print(commandText);
		
		try {
			if (!mConfigExec.execute(commandText)) {
				
				if (!mMainExec.execute(commandText)) {
					print("wrong input");
				}
			}
		} catch (Exception e) {
			String msg = e.getCause() == null ? 
					e.getMessage() : e.getCause().getMessage();
			print("error: " + msg);
		}
		
		print();
	}

	public void runService() {
		startService(IntentFor.commandsService(this));
		bindService(IntentFor.commandsService(this), 
				mConnection, BIND_AUTO_CREATE);
	}

	public void shutDownService() {
		if (mBound) {
			mService.stopForeground(true);
			unbindService(mConnection);
			mBound = false;
		}
		
		stopService(IntentFor.commandsService(this));
	}

    public void writeOutput(String text) {
    	mOutput.append(text);
    	
    	// Shorten the output text if it is too big
    	if (mLinesCount++ >= 2000) {
    		int length = mOutput.getText().length();
			mOutput.setText(mOutput.getText()
					.subSequence(length / 2, length - 1));
    		mLinesCount = 0;
    	}
    	
		scrollToEnd();
    }
    
    public void clearOutput() {
    	mOutput.setText("");
    	mResizeRequired = false;
		setScrollHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    
    private void scrollToEnd() {
    	mScroll.post(new Runnable() {
			@Override
			public void run() {
				mScroll.fullScroll(View.FOCUS_DOWN);
			}
		});
    }
    
    private void setInputFocus() {
    	mInput.post(new Runnable() {
    		@Override
    		public void run() {
				mInput.requestFocus();
    		}
    	});
    }
	
	private void resizeControls() {
		if (!mResizeRequired)
			mResizeRequired = mInput.getHeight() < mOutput.getLineHeight();
		
		if (mResizeRequired)
    		setScrollHeight(mLayout.getHeight() - (mLayout.getHeight() / 5));
	}
    
    private void setScrollHeight(int height) {
    	ViewGroup.LayoutParams lp = mScroll.getLayoutParams();
    	
    	if (height == 0 || lp.height == height)
    		return;
    	
    	lp.height = height;
    	mScroll.setLayoutParams(lp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        createMenu(menu);
        return true;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    		ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
        createMenu(menu);
    }

	private void createMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	return onMenuItemSelected(item);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	return onMenuItemSelected(item);
    }

	private boolean onMenuItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_exit) {
			exit();
		}
		return true;
	}

	public void exit() {
		if (RemoteControlService.isRunning)	
			finish();
		else
			System.exit(0);
	}
}
