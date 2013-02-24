package com.rc;

import static com.rc.base.Output.print;
import static com.rc.base.Output.printnb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;

import com.rc.base.CommandFunc;
import com.rc.base.Executor;
import com.rc.base.ParamFunc;
import com.rc.ui.MainActivity;
import com.rc.ui.R;
import com.rc.ui.RemoteControlService;

public class ConfigExecutor extends Executor {
	
	private MainActivity mActivity;
	
	public ConfigExecutor(MainActivity activity) {
		super(activity);
		
		mActivity = activity;
		
		registerCommand("?", new CommandFunc() {
			
			@Override
			public void execute() {
				ConfigExecutor.this.showHelp();
			}
		});
		
		registerCommand("cls", new CommandFunc() {
			
			@Override
			public void execute() {
				ConfigExecutor.this.clearScreen();
			}
		});
		
		registerCommand("ex", new CommandFunc() {
			
			@Override
			public void execute() {
				ConfigExecutor.this.exit();
			}
		});
		
		registerCommand("rr", new CommandFunc() {
			
			@Override
			public void execute() {
				ConfigExecutor.this.runRemoteControl();
			}
		});
		
		registerCommand("sr", new CommandFunc() {
			
			@Override
			public void execute() {
				ConfigExecutor.this.stopRemoteControl();
			}
		});
		
		registerParam("ip", new ParamFunc() {
			
			@Override
			public void execute(String value) {
				ConfigExecutor.this.setIp(value);
			}
		});
		
		registerParam("port", new ParamFunc() {
			
			@Override
			public void execute(String value) {
				ConfigExecutor.this.setPort(value);
			}
		});
		
		registerParam("rec", new ParamFunc() {
			
			@Override
			public void execute(String value) {
				ConfigExecutor.this.setReconnectInterval(value);
			}
		});
		
		registerParam("sup", new ParamFunc() {
			
			@Override
			public void execute(String value) {
				ConfigExecutor.this.setStartup(value);
			}
		});
		
		registerParam("nf", new ParamFunc() {
			
			@Override
			public void execute(String value) {
				ConfigExecutor.this.setNumberFormat(value);
			}
		});
	}

	private void showHelp() {
		BufferedReader reader = null;
		String line;

		try {
			InputStreamReader isr = new InputStreamReader(mContext
					.getResources().openRawResource(R.raw.help));
			reader = new BufferedReader(isr);
			while ((line = reader.readLine()) != null)
				print(line);
			reader.close();
			isr.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void clearScreen() {
		mActivity.clearOutput();
	}

	private void exit() {
		mActivity.exit();
	}

	private void setIp(String value) {
		
		String pattern = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
		String errorText = "'ip' must be valid IP address (127.0.0.1)";
		validateString(value, pattern, errorText);
		
		mPrefs.setIp(value);
		print("ip set to " + value);
	}

	private void setPort(String value) {
		int port = 0;
		
		try {
			port = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterException("'port' must be numeric int value");
		}
		
		mPrefs.setPort(port);
		print("port set to " + port);
	}

	private void setReconnectInterval(String value) {
		int interval = 0;
		
		try {
			interval = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterException("'reconnect interval' must be numeric int value");
		}
		
		mPrefs.setReconnectInterval(interval);
		print("reconnect interval set to " + interval);
	}

	private void setStartup(String value) {
		String pattern = "m|b";
		String errorText = "expected value 'm' for manual boot, " 
				+ "'b' for system boot";
		validateString(value, pattern, errorText);
		
		mPrefs.setRunOnBoot("b".equals(value));
		print("starts on boot " + (mPrefs.runOnBoot() ? "on" : "off"));
	}

	private void setNumberFormat(String value) {
		String pattern = "dec|oct|hex|bin";
		String errorText = "expected valid number format: dec|oct|hex|bin";
		validateString(value, pattern, errorText);
		
		mPrefs.setNumFormat(value);
		print("number format set to '" + value + "'");
	}

	private void runRemoteControl() {
		if (RemoteControlService.isRunning) {
			print("already running");
			return;
		}
		
		printnb("running remote control");
		mActivity.shutDownService();
		mActivity.runService();
	}

	private void stopRemoteControl() {
		if (!RemoteControlService.isRunning) {
			print("already stopped");
			return;
		}
		
		mActivity.shutDownService();
		print("remote control stopped");
	}
}
