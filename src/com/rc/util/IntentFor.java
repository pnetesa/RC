package com.rc.util;

import android.content.Context;
import android.content.Intent;

import com.rc.ui.RemoteControlService;
import com.rc.ui.MainActivity;

public class IntentFor {
	
	public static Intent mainActivity(Context context) { 
		return new Intent(context, MainActivity.class);
	}

	public static Intent commandsService(Context context) {
		return new Intent(context, RemoteControlService.class);
	}
}
