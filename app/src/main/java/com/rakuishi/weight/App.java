package com.rakuishi.weight;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.jakewharton.threetenabp.AndroidThreeTen;

import timber.log.Timber;

public class App extends Application {

    private Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidThreeTen.init(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.global_tracker);
        }
        return tracker;
    }
}
