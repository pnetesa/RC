package com.rc.base;

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

import com.rc.util.Preferences;

public abstract class Executor {
	
	protected Context mContext;
	protected Preferences mPrefs;
	
	private HashMap<String, CommandFunc> mCommands = 
			new HashMap<String, CommandFunc>();
	
	private HashMap<String, ParamFunc> mParams = 
			new HashMap<String, ParamFunc>();
	
	public Executor(Context context) {
		
		mContext = context;
		mPrefs = Preferences.getInstance(mContext);
	}
	
	public void registerCommand(String commandName, CommandFunc command) {
		mCommands.put(commandName, command);
	}
	
	public void registerParam(String paramName, ParamFunc param) {
		mParams.put(paramName, param);
	}
	
	public boolean execute(String commandText) {
		
		// Process command
		CommandFunc commandFunc = mCommands.get(commandText);
		if (commandFunc != null) {
			commandFunc.execute();
			return true;
		}
		
		// Specified text is not a command
		// Try process param...
		String[] parts = commandText.split(" ");
		
		if (parts.length >= 2) {
			
			String param = parts[0];
			String value = parts[1];
			
			ParamFunc paramFunc = mParams.get(param);
			if (paramFunc != null) {
				paramFunc.execute(value);
				return true;
			}
		}
		
		return false;
	}
	
	//
	// Utils
	//
	
	protected void validateString(String value, String pattern, String errorText) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(value);
		if (!m.matches())
			throw new IllegalArgumentException(errorText);
	}
	

	protected String formatNumbers(byte[] bytes) {
		
		String numFormat = mPrefs.numberFormat();
		
		if ("dec".equals(numFormat)) {
			
			return Arrays.toString(bytes);
			
		} else {
			
			String[] nums = new String[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				String num;
				
				if ("hex".equals(numFormat))
					num = "0x" + Integer.toHexString(bytes[i] & 0xFF).toUpperCase();
				else if ("oct".equals(numFormat))
					num = "0" + Integer.toOctalString(bytes[i] & 0xFF);
				else
					num = Integer.toBinaryString(bytes[i] & 0xFF);
				
				nums[i] = num;
			}
			
			return Arrays.toString(nums);
		}
	}

	protected byte[] toBytes(String values) {
		String[] nums = values.split(",");
		byte[] writeArgs = new byte[nums.length];
		int radix = getRadix();
		for (int i = 0; i < nums.length; i++)
			writeArgs[i] = Byte.parseByte(nums[i], radix);
		return writeArgs;
	}
	
	private int getRadix() {
		String name = mPrefs.numberFormat();
		
		if ("hex".equals(name))
			return 16;
		else if ("dec".equals(name))
			return 10;
		else if ("oct".equals(name))
			return 8;
		else // "bin"
			return 2;
	}
}
