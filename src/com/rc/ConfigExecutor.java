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
		
		registerCommand("cf", new CommandFunc() {
			
			@Override
			public void execute() {
				ConfigExecutor.this.showConfig();
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
		
		registerParam("fs", new ParamFunc() {
			
			@Override
			public void execute(String value) {
				ConfigExecutor.this.setFrameSize(value);
			}
		});
		
		registerParam("sk", new ParamFunc() {
			
			@Override
			public void execute(String value) {
				ConfigExecutor.this.setSkipCount(value);
			}
		});
		
		registerParam("dm", new ParamFunc() {
			
			@Override
			public void execute(String value) {
				ConfigExecutor.this.setDetectMethod(value);
			}
		});
		
		registerParam("dr", new ParamFunc() {
			
			@Override
			public void execute(String value) {
				ConfigExecutor.this.setDetectRate(value);
			}
		});
		
		registerParam("dd", new ParamFunc() {
			
			@Override
			public void execute(String value) {
				ConfigExecutor.this.setDetectDistance(value);
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

	private void showConfig() {
		print("config settings:");
		print();
		print(String.format("\taddress: %s:%d", mPrefs.host(), mPrefs.port()));
		print(String.format("\treconnect: %d second(s)", mPrefs.reconnectInterval()));
		print(String.format("\tstarts on boot: %s", mPrefs.runOnBoot() ? "on" : "off"));
		print(String.format("\tnumber format: '%s'", mPrefs.numberFormat()));
		print();
		print("\tvideo detector:");
		print();
		print(String.format("\t\tframe size: %s", 
			("l".equals(mPrefs.frameSize()) ? "800x600" : 
				("m".equals(mPrefs.frameSize()) ? "640x480" : "320x240"))));
		print(String.format("\t\tskip frames: %d", mPrefs.skipCount()));
		print(String.format("\t\tdetect method: %s", 
				("o".equals(mPrefs.detectMethod()) ? "ORB" : 
					("b".equals(mPrefs.detectMethod()) ? "BRISK" : "FAST"))));
		print(String.format("\t\tdetect rate: %d", mPrefs.detectRate()));
		print(String.format("\t\tdetect distance: %d", mPrefs.detectDistance()));
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

	private void setFrameSize(String value) {
		String pattern = "l|m|s";
		String errorText = "expected value 'l' - 800x600, " 
				+ "'m' - 640x480 or 's' - 320x240";
		validateString(value, pattern, errorText);
		
		mPrefs.setFrameSize(value);
		print("frame size set to " + 
				("l".equals(value) ? "800x600" : 
					("m".equals(value) ? "640x480" : "320x240")));
	}

	private void setSkipCount(String value) {
		int skipCount = 0;
		
		try {
			skipCount = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterException("'skip count' must be numeric int value");
		}
		
		if (skipCount <= 0)
			throw new InvalidParameterException("'skip count' must be positive int value");
		
		mPrefs.setSkipCount(skipCount);
		print("skip frame count set to " + skipCount);
	}

	private void setDetectMethod(String value) {
		String pattern = "o|b|f";
		String errorText = "expected value: 'o' - ORB, 'b' - BRISK, 'f' - FAST";
		validateString(value, pattern, errorText);
		
		mPrefs.setDetectMethod(value);
		print("detect method set to " + 
				("o".equals(value) ? "ORB" : 
					("b".equals(value) ? "BRISK" : "FAST")));
	}
	
	private void setDetectRate(String value) {
		int detectRate = 0;
		
		try {
			detectRate = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterException("'detect rate' must be numeric int value");
		}
		
		if (detectRate <= 0 || detectRate >= 16)
			throw new InvalidParameterException("expected int value between 1 and 15 inclusively");
		
		mPrefs.setDetectRate(detectRate);
		print("detect rate set to " + detectRate);
	}

	private void setDetectDistance(String value) {
		int detectDistance = 0;
		
		try {
			detectDistance = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterException("'detect distance' must be numeric int value");
		}
		
		if (detectDistance <= 0)
			throw new InvalidParameterException("'detect distance' must be positive int value");
		
		mPrefs.setDetectDistance(detectDistance);
		print("detect distance set to " + detectDistance);
	}
}
