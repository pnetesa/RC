package com.rc;

import java.util.Arrays;

import android.content.Context;

import static com.rc.base.Output.*;
import com.rc.base.CommandFunc;
import com.rc.base.Executor;
import com.rc.base.ParamFunc;
import com.rc.util.UsbConnector;

public class MainExecutor extends Executor {
	
	private static MainExecutor mInstance;
	
	private static UsbConnector mUsbConnector;
	private static byte[] mWriteArgs;
	
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
		print(Arrays.toString(bytes));
	}

	private void pressUp() {
		print("You pressed UP key!");
	}

	private void setS(String values) {
		String[] nums = values.split(",");
		mWriteArgs = new byte[nums.length];
		for (int i = 0; i < nums.length; i++)
			mWriteArgs[i] = Byte.parseByte(nums[i]);
		print("set 's' to " + 
				(mWriteArgs.length == 1 ? 
						mWriteArgs[0] : Arrays.toString(mWriteArgs)));
	}
}
