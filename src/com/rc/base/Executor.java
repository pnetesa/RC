package com.rc.base;

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
	
	protected void validateString(String value, String pattern, String errorText) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(value);
		if (!m.matches())
			throw new IllegalArgumentException(errorText);
	}
}
