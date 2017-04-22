package com.rakuishi.weight;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

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
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractValuesFromIntent(getIntent());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        getSupportActionBar().setTitle(R.string.edit);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        compositeDisposable = new CompositeDisposable();
        client = new FitnessClient(this, this);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.save:
                save();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // region FitnessClient.Callback

    @Override
    public void onConnectionSuccess() {
        Timber.d("onConnectionSuccess");
        Field field = dataPoint.getDataType().getFields().get(0);
        String value = dataPoint.getValue(field).toString();
        binding.weightEditText.setText(value);
        binding.weightEditText.setHint(value);
        binding.weightEditText.setSelection(0, value.length());

        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(binding.weightEditText, InputMethodManager.SHOW_IMPLICIT);
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

    private void save() {
        // do something
    }
}
