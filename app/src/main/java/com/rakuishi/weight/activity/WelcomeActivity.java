package com.rakuishi.weight.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.google.android.gms.common.SignInButton;
import com.rakuishi.weight.R;
import com.rakuishi.weight.databinding.ActivityWelcomeBinding;
import com.rakuishi.weight.pref.DefaultPrefs;

public class WelcomeActivity extends BaseActivity {

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
            DefaultPrefs.setShowWelcome(this, false);
            startActivity(MainActivity.create(this));
        });
    }
}
