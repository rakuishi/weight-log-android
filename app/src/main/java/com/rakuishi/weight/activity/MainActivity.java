package com.rakuishi.weight.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.fitness.data.DataPoint;
import com.rakuishi.weight.BuildConfig;
import com.rakuishi.weight.R;
import com.rakuishi.weight.databinding.ActivityMainBinding;
import com.rakuishi.weight.pref.DefaultPrefs;
import com.rakuishi.weight.repo.FitnessClient;
import com.rakuishi.weight.view.EmptyAdapter;
import com.rakuishi.weight.view.FitnessWeightAdapter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements
        FitnessClient.Callback, View.OnClickListener, FitnessWeightAdapter.Callback {

    private FitnessClient client;
    private CompositeDisposable compositeDisposable;
    private ActivityMainBinding binding;
    private FitnessWeightAdapter fitnessWeightAdapter;
    private EmptyAdapter emptyAdapter;
    // TODO: Store last selected position
    private int selectedPosition = 0;

    public static Intent create(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        compositeDisposable = new CompositeDisposable();

        if (DefaultPrefs.showWelcome(this)) {
            startActivity(WelcomeActivity.create(this));
            finish();
            return;
        }

        fitnessWeightAdapter = new FitnessWeightAdapter(this, this, selectedPosition);
        emptyAdapter = new EmptyAdapter(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setIcon(R.drawable.icon);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(fitnessWeightAdapter);
        binding.signInButton.setSize(SignInButton.SIZE_WIDE);
        binding.signInButton.setOnClickListener(this);
        binding.fab.setOnClickListener(this);

        client = new FitnessClient(this, this);
        showAd();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (client.isConnected()) {
            getMenuInflater().inflate(R.menu.activity_main, menu);
            checkMenuItemIfNeeded(menu.findItem(R.id.last_30_days));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(SettingsActivity.create(this));
                break;
        }

        checkMenuItemIfNeeded(item);
        return super.onOptionsItemSelected(item);
    }

    private void checkMenuItemIfNeeded(MenuItem item) {
        int position = getPositionFromCheckableMenuItem(item.getItemId());
        if (position != -1) {
            item.setChecked(true);
            selectedPosition = position;
            binding.progressBar.setVisibility(View.VISIBLE);
            fitnessWeightAdapter.setAmount(getAmount(selectedPosition));
            loadFitnessWeight(getAmount(selectedPosition));
        }
    }

    private int getPositionFromCheckableMenuItem(int id) {
        switch (id) {
            case R.id.last_30_days:
                return 0;
            case R.id.last_90_days:
                return 1;
            case R.id.last_180_days:
                return 2;
            case R.id.last_360_days:
                return 3;
            default:
                return -1;
        }
    }

    private int getAmount(int position) {
        switch (position) {
            case 0:
                return 1;
            case 1:
                return 3;
            case 2:
                return 6;
            case 3:
                return 12;
            default:
                return -1;
        }
    }

    // region FitnessClient.Callback

    @Override
    public void onConnectionSuccess() {
        loadFitnessWeight(getAmount(selectedPosition));
        invalidateOptionsMenu();
        binding.fab.setVisibility(View.VISIBLE);
        binding.signInLayout.setVisibility(View.GONE);
    }

    @Override
    public void onConnectionFail(Exception e) {
        invalidateOptionsMenu();
        binding.progressBar.setVisibility(View.GONE);
        binding.signInLayout.setVisibility(View.VISIBLE);
        showGooglePlayServicesDialogIfAvailable();
    }

    // endregion

    // region View.OnClickListener

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                startActivity(MainActivity.create(this));
                finish();
                break;
            case R.id.fab:
                startActivity(EditActivity.create(this));
                break;
        }
    }

    // endregion

    // region FitnessWeightAdapter.Callback

    @Override
    public void onDataPointClicked(DataPoint dataPoint) {
        startActivity(EditActivity.create(this, dataPoint));
    }

    // endregion

    private void loadFitnessWeight(int amount) {
        Disposable disposable = client.find(amount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(dataPoints -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (dataPoints == null || dataPoints.size() == 0) {
                        updateRecyclerViewPaddingBottom(true);
                        binding.recyclerView.setAdapter(emptyAdapter);
                    } else {
                        updateRecyclerViewPaddingBottom(false);
                        binding.recyclerView.setAdapter(fitnessWeightAdapter);
                        fitnessWeightAdapter.setDataPoints(dataPoints);
                    }
                });
        compositeDisposable.add(disposable);
    }

    private void updateRecyclerViewPaddingBottom(boolean isEmpty) {
        binding.recyclerView.setPadding(
                0, 0, 0, isEmpty ? 0 : (int) getResources().getDimension(R.dimen.dp_72)
        );
    }

    /**
     * Google Play Services に関する問題でユーザーが解決可能ならば、ダイアログを表示する
     * https://developers.google.com/android/reference/com/google/android/gms/common/GoogleApiAvailability
     */
    private void showGooglePlayServicesDialogIfAvailable() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int code = availability.isGooglePlayServicesAvailable(this);
        if (availability.isUserResolvableError(code)) {
            availability.getErrorDialog(this, 1, 2).show();
        }
    }

    private void showAd() {
        AdRequest request;
        if (BuildConfig.DEBUG) {
            request = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                    .addTestDevice("AC98C820A50B4AD8A2106EDE96FB87D4")  // An example device ID
                    .build();
        } else {
            request = new AdRequest.Builder().build();
        }

        binding.adView.loadAd(request);
    }
}
