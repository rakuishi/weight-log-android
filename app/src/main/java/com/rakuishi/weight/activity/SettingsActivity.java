package com.rakuishi.weight.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.rakuishi.weight.R;
import com.rakuishi.weight.util.SignInHelper;
import com.rakuishi.weight.databinding.ActivitySettingsBinding;
import com.rakuishi.weight.repo.FitnessClient;

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
        binding.bombButton.setOnClickListener(v -> {
            if (client.isConnected()) {
                client.clearDefaultAccountAndReconnect();
            }
        });

        getSupportActionBar().setTitle(R.string.settings);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
