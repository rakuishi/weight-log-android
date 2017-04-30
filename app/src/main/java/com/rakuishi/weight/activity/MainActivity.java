package com.rakuishi.weight.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.fitness.data.DataPoint;
import com.rakuishi.weight.R;
import com.rakuishi.weight.databinding.ActivityMainBinding;
import com.rakuishi.weight.repo.FitnessClient;
import com.rakuishi.weight.view.FitnessWeightAdapter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements
        FitnessClient.Callback, View.OnClickListener, FitnessWeightAdapter.Callback {

    private FitnessClient client;
    private CompositeDisposable compositeDisposable;
    private ActivityMainBinding binding;
    private FitnessWeightAdapter adapter;

    public static Intent create(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        adapter = new FitnessWeightAdapter(this, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.signInButton.setSize(SignInButton.SIZE_WIDE);
        binding.signInButton.setOnClickListener(this);
        binding.fab.setOnClickListener(this);

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
        if (client.isConnected()) {
            getMenuInflater().inflate(R.menu.activity_main, menu);
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

        return super.onOptionsItemSelected(item);
    }

    // region FitnessClient.Callback

    @Override
    public void onConnectionSuccess() {
        loadFitnessWeight(1);
        invalidateOptionsMenu();
        binding.fab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionFail(Exception e) {
        binding.progressBar.setVisibility(View.GONE);
        binding.signInButton.setVisibility(View.VISIBLE);
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

    @Override
    public void onSpinnerItemSelected(int amount) {
        binding.progressBar.setVisibility(View.VISIBLE);
        loadFitnessWeight(amount);
    }

    // endregion

    private void loadFitnessWeight(int amount) {
        Disposable disposable = client.find(amount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(dataPoints -> {
                    binding.progressBar.setVisibility(View.GONE);
                    adapter.setDataPoints(dataPoints);
                });
        compositeDisposable.add(disposable);
    }
}
