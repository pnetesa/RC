package com.rc.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

	private static Preferences mInstance;

	private Context mContext;

	public static Preferences getInstance(Context context) {

		if (mInstance == null)
			mInstance = new Preferences(context);

		return mInstance;
	}

	private Preferences(Context context) {
		mContext = context;
	}

	private SharedPreferences getPrefs() {
		return mContext.getSharedPreferences(Consts.PREFS_NAME,
				Context.MODE_PRIVATE);
	}

	private void saveString(String key, String value) {
		SharedPreferences prefs = getPrefs();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}

	private void saveInt(String key, int value) {
		SharedPreferences prefs = getPrefs();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	private void saveBoolean(String key, boolean value) {
		SharedPreferences prefs = getPrefs();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	//
	// Setters
	//

	public void setIp(String ip) {
		saveString(Keys.IP_PREF, ip);
	}

	public void setPort(int port) {
		saveInt(Keys.PORT_PREF, port);
	}

	public void setReconnectInterval(int interval) {
		saveInt(Keys.RECONN_INTERVAL_PREF, interval);
	}

	public void setRunOnBoot(boolean runOnBoot) {
		saveBoolean(Keys.RUN_ON_BOOT_PREF, runOnBoot);
	}
	
	public void setNumFormat(String numFormat) {
		saveString(Keys.NUMBER_FORMAT_PREF, numFormat);
	}
	
	//
	// Getters
	//

	public String host() {
		return getPrefs().getString(Keys.IP_PREF, "0.0.0.0");
	}

	public int port() {
		return getPrefs().getInt(Keys.PORT_PREF, 0);
	}

	public int reconnectInterval() {
		return getPrefs().getInt(Keys.RECONN_INTERVAL_PREF, 10);
	}

	public boolean runOnBoot() {
		return getPrefs().getBoolean(Keys.RUN_ON_BOOT_PREF, false);
	}
	
	public String numberFormat() {
		return getPrefs().getString(Keys.NUMBER_FORMAT_PREF, "dec");
	}
}
