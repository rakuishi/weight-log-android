package com.rakuishi.weight.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

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
import com.rakuishi.weight.view.SpinnerAdapter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements
        FitnessClient.Callback, View.OnClickListener, FitnessWeightAdapter.Callback {

    private static final int AMOUNT_POSITION_MIN = 0;
    private static final int AMOUNT_POSITION_MAX = 3;

    private FitnessClient client;
    private CompositeDisposable compositeDisposable;
    private ActivityMainBinding binding;
    private FitnessWeightAdapter fitnessWeightAdapter;
    private EmptyAdapter emptyAdapter;
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

        int position = DefaultPrefs.getAmountPosition(this);
        selectedPosition = Math.max(AMOUNT_POSITION_MIN, Math.min(AMOUNT_POSITION_MAX, position));
        fitnessWeightAdapter = new FitnessWeightAdapter(this, this, selectedPosition);
        emptyAdapter = new EmptyAdapter(this);

        setSupportActionBar(binding.toolbar);
        // noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        String[] objects = new String[]{
                getString(R.string.last_30_days),
                getString(R.string.last_90_days),
                getString(R.string.last_180_days),
                getString(R.string.last_360_days)
        };

        SpinnerAdapter adapter = new SpinnerAdapter(MainActivity.this, objects);
        binding.spinner.setEnabled(false);
        binding.spinner.setAdapter(adapter);
        binding.spinner.setSelection(selectedPosition);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                DefaultPrefs.setAmountPosition(MainActivity.this, position);

                if (client.isConnected()) {
                    loadFitnessWeight(getAmount(selectedPosition));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(SettingsActivity.create(this));
                break;
        }

        return super.onOptionsItemSelected(item);
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
        binding.spinner.setEnabled(true);
        loadFitnessWeight(getAmount(selectedPosition));
        binding.fab.setVisibility(View.VISIBLE);
        binding.signInLayout.setVisibility(View.GONE);
    }

    @Override
    public void onConnectionFail(Exception e) {
        binding.spinner.setEnabled(false);
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
        binding.progressBar.setVisibility(View.VISIBLE);

        fitnessWeightAdapter.setAmount(amount);
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
