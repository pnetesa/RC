package com.rc.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rc.util.IntentFor;
import com.rc.util.Preferences;

public class BootReceiver extends BroadcastReceiver {
	
	public static final String TAG = BootReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Preferences prefs = Preferences.getInstance(context);
		if (!prefs.runOnBoot())
			return;
		
		Log.i(TAG, "Starting service on boot");
		context.startService(IntentFor.commandsService(context));
	}
}
