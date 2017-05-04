package com.rakuishi.weight.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.rakuishi.weight.App;

import timber.log.Timber;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendScreenName(this);
    }

    public void sendScreenName(AppCompatActivity activity) {
        Tracker tracker = ((App) getApplication()).getDefaultTracker();
        tracker.setScreenName(activity.getClass().getSimpleName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
