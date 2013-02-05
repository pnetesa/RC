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

	public String host() {
		SharedPreferences prefs = getPrefs();
		return prefs.getString(Keys.IP_PREF, "0.0.0.0");
	}

	public int port() {
		SharedPreferences prefs = getPrefs();
		return prefs.getInt(Keys.PORT_PREF, 0);
	}

	public int reconnectInterval() {
		SharedPreferences prefs = getPrefs();
		return prefs.getInt(Keys.RECONN_INTERVAL_PREF, 10);
	}

	public boolean runOnBoot() {
		SharedPreferences prefs = getPrefs();
		return prefs.getBoolean(Keys.RUN_ON_BOOT_PREF, false);
	}
}
