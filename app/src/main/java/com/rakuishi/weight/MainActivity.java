package com.rakuishi.weight;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.fitness.data.DataPoint;
import com.rakuishi.weight.databinding.ActivityMainBinding;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

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
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getResources()));
        binding.recyclerView.setAdapter(adapter);
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

    // region FitnessClient.Callback

    @Override
    public void onConnectionSuccess() {
        loadFitnessWeight();
    }

    @Override
    public void onConnectionFail(Exception e) {
        SignInHelper.onConnectionFail(this);
    }

    // endregion

    // region View.OnClickListener

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            startActivity(EditActivity.create(this));
        }
    }

    // endregion

    // region FitnessWeightAdapter.Callback

    @Override
    public void onClickDataPoint(DataPoint dataPoint) {
        startActivity(EditActivity.create(this, dataPoint));
    }

    // endregion

    private void loadFitnessWeight() {
        Disposable disposable = client.find(3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(dataPoints -> {
                    binding.progressBar.setVisibility(View.GONE);
                    adapter.setDataPoints(dataPoints);
                });
        compositeDisposable.add(disposable);
    }
}
