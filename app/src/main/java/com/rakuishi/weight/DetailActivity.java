package com.rakuishi.weight;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.rakuishi.weight.databinding.ActivityDetailBinding;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class DetailActivity extends AppCompatActivity implements
        FitnessClient.Callback {

    private final static String KEY_DATA_POINT = "dataPoint";
    private CompositeDisposable compositeDisposable;
    private ActivityDetailBinding binding;
    private FitnessClient client;
    private DataPoint dataPoint;

    public static Intent create(Context context, DataPoint dataPoint) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(KEY_DATA_POINT, dataPoint);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractValuesFromIntent(getIntent());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        compositeDisposable = new CompositeDisposable();
        client = ((App) getApplication()).getFitnessClient();
        client.onCreate(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.onStop();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        client.onActivityResult(requestCode, resultCode, data);
    }

    // region FitnessClient.Callback

    @Override
    public void onConnectionSuccess() {
        Timber.d("onConnectionSuccess");
        Field field = dataPoint.getDataType().getFields().get(0);
        binding.weightEditText.setText(dataPoint.getValue(field).toString());
    }

    @Override
    public void onConnectionFail(Exception e) {
        Timber.d("onConnectionFail: " + e.getMessage());
    }

    // endregion

    private void extractValuesFromIntent(Intent intent) {
        if (!intent.hasExtra(KEY_DATA_POINT)) {
            // something happened
            finish();
        }

        dataPoint = getIntent().getParcelableExtra(KEY_DATA_POINT);
    }
}
