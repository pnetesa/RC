package com.rc.base;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

public abstract class Executor {
	
	protected Context mContext;
	
	private LinkedHashMap<String, String> mParamValues = 
			new LinkedHashMap<String, String>();
	
	private HashMap<String, CommandFunc> mCommands = 
			new HashMap<String, CommandFunc>();
	
	private HashMap<String, ParamFunc> mParams = 
			new HashMap<String, ParamFunc>();
	
	public Executor(Context context, boolean useParamLog) {
		mContext = context;
		
		if (!useParamLog)
			return;
		
		registerCommand("l", new CommandFunc() {
			
			@Override
			public void execute() {
				printLog();
			}
		});
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
				logValue(param, value);
				return true;
			}
		}
		
		return false;
	}

	private void printLog() {
		Set<Entry<String, String>> paramValues = mParamValues.entrySet();
		for (Entry<String, String> entry : paramValues) {
			String param = entry.getKey();
			String value = entry.getValue();
			Output.print(String.format("\t%s: %s", param, value));
		}
	}

	private void logValue(String param, String value) {
		if (mParamValues.containsKey(param))
			mParamValues.remove(param);
		
		mParamValues.put(param, value);
	}
	
	protected void validateString(String value, String pattern, String errorText) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(value);
		if (!m.matches())
			throw new IllegalArgumentException(errorText);
	}
}
