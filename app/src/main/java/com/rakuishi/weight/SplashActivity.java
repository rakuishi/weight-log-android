package com.rakuishi.weight;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.common.SignInButton;
import com.rakuishi.weight.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity implements
        FitnessClient.Callback, View.OnClickListener {

    private ActivitySplashBinding binding;
    private FitnessClient client;

    public static Intent create(Context context) {
        return new Intent(context, SplashActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        binding.signInButton.setOnClickListener(this);
        binding.signInButton.setSize(SignInButton.SIZE_WIDE);
        getSupportActionBar().hide();

        client = new FitnessClient(this, this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    // region FitnessClient.Callback

    @Override
    public void onConnectionSuccess() {
        startActivity(MainActivity.create(this));
        finish();
    }

    @Override
    public void onConnectionFail(Exception e) {
        binding.progressBar.setVisibility(View.GONE);
        binding.signInButton.setVisibility(View.VISIBLE);
    }

    // endregion

    // View.OnClickListener

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button) {
            startActivity(SplashActivity.create(this));
            finish();
        }
    }

    // endregion
}
