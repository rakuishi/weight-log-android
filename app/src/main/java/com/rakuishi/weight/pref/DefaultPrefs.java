package com.rakuishi.weight.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.rakuishi.weight.BuildConfig;

public class DefaultPrefs {

    private static final String prefix = BuildConfig.APPLICATION_ID + ".";
    private static final String show_welcome = prefix + "welcome";

    public static boolean showWelcome(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(show_welcome, true);
    }

    public static void setShowWelcome(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(show_welcome, value);
        editor.apply();
    }
}
