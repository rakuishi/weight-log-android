package com.rakuishi.weight;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.rakuishi.weight.databinding.ActivityEditBinding;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

public class EditActivity extends AppCompatActivity implements FitnessClient.Callback {

    private final static String KEY_DATA_POINT = "dataPoint";
    private CompositeDisposable compositeDisposable;
    private ActivityEditBinding binding;
    private FitnessClient client;
    private DataPoint dataPoint;
    private long timestamp;
    private boolean isEditable;

    public static Intent create(Context context) {
        return new Intent(context, EditActivity.class);
    }

    public static Intent create(Context context, DataPoint dataPoint) {
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra(KEY_DATA_POINT, dataPoint);
        return intent;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit);

        extractValuesFromIntent(getIntent());
        isEditable = (dataPoint == null);
        timestamp = (dataPoint == null) ? (new Date()).getTime() : dataPoint.getTimestamp(TimeUnit.MILLISECONDS);

        getSupportActionBar().setTitle(dataPoint == null ? R.string.add_your_weight : R.string.edit_your_weight);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        compositeDisposable = new CompositeDisposable();
        client = new FitnessClient(this, this);

        setupViewComponents();
        updateViewComponentsStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (getCurrentFocus() != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail, menu);
        boolean enabled = isEditable && client.isConnected();
        menu.findItem(R.id.save).setEnabled(enabled);

        if (dataPoint == null) {
            menu.findItem(R.id.delete).setVisible(false);
        } else {
            menu.findItem(R.id.delete).setVisible(true);
            menu.findItem(R.id.delete).setEnabled(enabled);
        }

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
            case R.id.delete:
                delete();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // region FitnessClient.Callback

    @Override
    public void onConnectionSuccess() {
        isEditable = dataPoint == null || client.isEditable(dataPoint);
        invalidateOptionsMenu();
        updateViewComponentsStatus();
    }

    @Override
    public void onConnectionFail(Exception e) {
        Timber.d("onConnectionFail: " + e.getMessage());
    }

    // endregion

    // region View

    private void setupViewComponents() {
        String value = "";
        if (dataPoint != null) {
            Field field = dataPoint.getDataType().getFields().get(0);
            value = dataPoint.getValue(field).toString();
        }

        binding.weightEditText.setText(value);
        binding.weightEditText.setHint(value);
        binding.weightEditText.setSelection(0, value.length());

        binding.dateEditText.setText(getDateInstance().format(timestamp));
        binding.timeEditText.setText(getTimeInstance().format(timestamp));
    }

    private void updateViewComponentsStatus() {
        binding.noteTextView.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        binding.weightEditText.setFocusable(isEditable);
        binding.weightEditText.setClickable(isEditable);
        binding.weightEditText.setFocusableInTouchMode(isEditable);
        binding.weightEditText.setCursorVisible(isEditable);
        binding.progressBar.setVisibility(View.GONE);

        if (isEditable) {
            binding.weightEditText.requestFocus();
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.showSoftInput(binding.weightEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    // endregion

    private void extractValuesFromIntent(Intent intent) {
        if (intent.hasExtra(KEY_DATA_POINT)) {
            dataPoint = intent.getParcelableExtra(KEY_DATA_POINT);
        }
    }

    private void save() {
        // TODO: validate weight value
        float weight = Float.valueOf(binding.weightEditText.getText().toString());

        Completable completable = dataPoint == null
                ? client.insert(weight, timestamp)
                : client.update(weight, timestamp);

        Disposable disposable = completable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(() -> {
                    finish();
                }, throwable -> {
                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
        compositeDisposable.add(disposable);
    }

    private void delete() {
        Completable completable = client.delete(timestamp);

        Disposable disposable = completable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(() -> {
                    finish();
                }, throwable -> {
                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
        compositeDisposable.add(disposable);
    }
}
