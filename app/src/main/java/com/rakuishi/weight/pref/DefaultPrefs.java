package com.rakuishi.weight.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.rakuishi.weight.BuildConfig;

public class DefaultPrefs {

    private static final String prefix = BuildConfig.APPLICATION_ID + ".";
    private static final String show_welcome = prefix + "welcome";
    private static final String amount_position = prefix + "amount_position";

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

    public static int getAmountPosition(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(amount_position, 0);
    }

    public static void setAmountPosition(Context context, int position) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(amount_position, position);
        editor.apply();
    }
}
