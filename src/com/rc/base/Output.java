package com.rc.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.rc.util.Consts;
import com.rc.util.Preferences;

public class Output {
	
	private static List<OutputFunc> mOutputs = new ArrayList<OutputFunc>();
	
	public static void registerOutput(OutputFunc output) {
		synchronized (mOutputs) {
			mOutputs.add(output);
		}
	}
	
	public static void unregisterOutput(OutputFunc output) {
		synchronized (mOutputs) {
			mOutputs.remove(output);
		}
	}
	
	public static void printnb(String text) {
    	write(text);
    }
	
	public static void print(Object obj) {
		String text = obj == null ? "null" : obj.toString();
		print(text);
	}
    
	public static void print(String text) {
    	write(text + Consts.NEW_LINE);
    }
    
	public static void print() {
    	write(Consts.NEW_LINE);
    }
	
	public static void printError(Throwable e) {
		Preferences prefs = Preferences.getInstance();
		if (prefs == null || !prefs.traceOn())
			return;
		
		synchronized (Output.class) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			print("exception: " + sw.toString());
		}
	}
	
    private static void write(String text) {
		synchronized (mOutputs) {
			for (OutputFunc out : mOutputs)
				out.write(text);
		}
	}
}
