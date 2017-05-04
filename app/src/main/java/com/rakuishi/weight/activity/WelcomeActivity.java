package com.rakuishi.weight.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.SignInButton;
import com.rakuishi.weight.R;
import com.rakuishi.weight.databinding.ActivityWelcomeBinding;
import com.rakuishi.weight.pref.DefaultPrefs;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;

    public static Intent create(Context context) {
        return new Intent(context, WelcomeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding.signInButton.setSize(SignInButton.SIZE_WIDE);
        binding.signInButton.setOnClickListener(v -> {
            DefaultPrefs.setShowWelcom(this, false);
            startActivity(MainActivity.create(this));
        });
    }
}
