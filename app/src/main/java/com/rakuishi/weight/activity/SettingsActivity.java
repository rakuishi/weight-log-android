package com.rakuishi.weight.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.rakuishi.weight.BuildConfig;
import com.rakuishi.weight.R;
import com.rakuishi.weight.databinding.ActivitySettingsBinding;
import com.rakuishi.weight.repo.FitnessClient;
import com.rakuishi.weight.util.SignInHelper;

public class SettingsActivity extends AppCompatActivity implements FitnessClient.Callback {

    private ActivitySettingsBinding binding;
    private FitnessClient client;

    public static Intent create(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.bombTextView.setOnClickListener(v -> {
            if (client.isConnected()) {
                client.clearDefaultAccountAndReconnect();
            }
        });

        String version = String.format(getString(R.string.version_format), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        binding.versionTextView.setText(version);

        binding.developerTextView.setOnClickListener(v -> {
            Uri uri = Uri.parse(getString(R.string.developer_website));
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(SettingsActivity.this, uri);
        });

        client = new FitnessClient(this, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // region

    @Override
    public void onConnectionSuccess() {
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onConnectionFail(Exception e) {
        SignInHelper.fail(this);
    }

    // endregion
}
