package com.rc.util;

import android.content.Context;
import android.content.Intent;

import com.rc.ui.RemoteControlService;
import com.rc.ui.MainActivity;
import com.rc.ui.VideoDetectorActivity;

public class IntentFor {
	
	public static Intent mainActivity(Context context) { 
		return new Intent(context, MainActivity.class);
	}

	public static Intent videoDetectorActivity(Context context) { 
		return new Intent(context, VideoDetectorActivity.class);
	}
	
	public static Intent commandsService(Context context) {
		return new Intent(context, RemoteControlService.class);
	}
	
	public static Intent stopDetectorAction() {
		return new Intent(Consts.STOP_VIDEO_DETECTOR_ACTION);
	}
}
