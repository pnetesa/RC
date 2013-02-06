package com.rc;

import static com.rc.base.Output.print;

import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.rc.base.CommandFunc;
import com.rc.base.Executor;
import com.rc.base.ParamFunc;
import com.rc.util.UsbConnector;

public class MainExecutor extends Executor {
	
	public static final String TAG = MainExecutor.class.getSimpleName();

	private static MainExecutor mInstance;

	private static UsbConnector mUsbConnector;
	private static byte[] mWriteArgs;

	private Handler mHandler = new Handler();
	
	private ExecutorService mReadExecutor = 
			Executors.newSingleThreadExecutor();
	private Thread mReadThread;
	private DateFormat mDateFormat = 
			DateFormat.getTimeInstance(DateFormat.MEDIUM);

	public static MainExecutor getInstance(Context context) {

		if (mInstance == null)
			mInstance = new MainExecutor(context);

		return mInstance;
	}

	private MainExecutor(Context context) {
		super(context, true);
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

		registerParam("ir", new ParamFunc() {

			@Override
			public void execute(String value) {
				MainExecutor.this.setIntervalRead(value);
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

	private String formatNumbers(byte[] bytes) {
		
		String numFormat = mPrefs.numberFormat();
		
		if ("dec".equals(numFormat)) {
			
			return Arrays.toString(bytes);
			
		} else {
			
			String[] nums = new String[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				String num;
				
				if ("hex".equals(numFormat))
					num = "0x" + Integer.toHexString(bytes[i]).toUpperCase();
				else if ("oct".equals(numFormat))
					num = "0" + Integer.toOctalString(bytes[i]);
				else
					num = Integer.toBinaryString(bytes[i]);
				
				nums[i] = num;
			}
			
			return Arrays.toString(nums);
		}
	}

	private void halt() {
		
		setIntervalRead(String.valueOf(0)); // for example...
		
		print("halt!");
	}

	private void pressUp() {
		print("You pressed UP key!");
	}

	private void setS(String values) {
		String[] nums = values.split(",");
		byte[] writeArgs = new byte[nums.length];
		for (int i = 0; i < nums.length; i++)
			writeArgs[i] = Byte.parseByte(nums[i]);

		mWriteArgs = writeArgs;
		print("set 's' to "
				+ (writeArgs.length == 1 ? mWriteArgs[0] : Arrays
						.toString(mWriteArgs)));
	}

	private void setIntervalRead(String value) {
		int interval = 0;

		try {
			interval = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterException(
					"'interval read' must be numeric int value");
		}
		
		if (mReadThread != null) {
			mReadThread.interrupt();
			mReadThread = null;
		}
			
		if (interval == 0) {
			print("interval read off");
			return;
		}
		
		final long period = interval;
		mReadExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				
				mReadThread = Thread.currentThread();
				
				try {
					
					while (!Thread.interrupted()) {
						
						TimeUnit.SECONDS.sleep(period);
						MainExecutor.this.intervalReadSafe();
					}
					
				} catch (InterruptedException e) {
					Log.i(TAG, e.getLocalizedMessage(), e);
				}
			}
		});
		
		print("interval read: " + interval + " second(s)");
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
}
