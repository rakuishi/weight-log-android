package com.rakuishi.weight;

import android.app.Application;

import timber.log.Timber;

public class App extends Application {

    private FitnessClient fitnessClient = null;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public FitnessClient getFitnessClient() {
        if (fitnessClient == null) {
            fitnessClient = new FitnessClient(this);
        }

        return fitnessClient;
    }
}
