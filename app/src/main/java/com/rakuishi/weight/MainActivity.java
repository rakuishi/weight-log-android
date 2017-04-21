package com.rakuishi.weight;

import android.content.Intent;
import android.content.IntentSender;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.evernote.android.state.State;
import com.evernote.android.state.StateSaver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.rakuishi.weight.databinding.ActivityMainBinding;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final int REQUEST_OAUTH = 1;

    @State
    boolean authInProgress = false;

    private GoogleApiClient client = null;
    private CompositeDisposable compositeDisposable;
    private ActivityMainBinding binding;
    private FitnessWeightAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        StateSaver.restoreInstanceState(this, savedInstanceState);

        adapter = new FitnessWeightAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getResources()));
        binding.recyclerView.setAdapter(adapter);
        binding.fab.setOnClickListener(this);

        compositeDisposable = new CompositeDisposable();
        client = FitnessWeightHelper.buildGoogleApiClient(this, this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (client.isConnected()) {
            client.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK && !client.isConnecting() && !client.isConnected()) {
                // Make sure the app is not already connected or attempting to connect
                client.connect();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        StateSaver.restoreInstanceState(this, outState);
    }

    // region GoogleApiClient.ConnectionCallbacks

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        loadFitnessWeight();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.d("onConnectionSuspended");
    }

    // endregion

    // region GoogleApiClient.OnConnectionFailedListener

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.d("onConnectionFailed: " + connectionResult.toString());
        if (!connectionResult.hasResolution()) {
            // Show the localized error dialog
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(),
                    MainActivity.this, 0).show();
            return;
        }

        if (!authInProgress) {
            try {
                Timber.d("Attempting to resolve failed connection");
                authInProgress = true;
                connectionResult.startResolutionForResult(MainActivity.this, REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {
                Timber.d(e.getMessage());
            }
        }
    }

    // endregion

    // region View.OnClickListener

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            Disposable disposable = FitnessWeightHelper.insert(MainActivity.this, client, 60.f)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(() -> {
                        loadFitnessWeight();
                    }, throwable -> {
                        Timber.d(throwable.getMessage());
                    });
            compositeDisposable.add(disposable);
        }
    }

    // endregion

    private void loadFitnessWeight() {
        Disposable disposable = FitnessWeightHelper.find(client, 3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(dataPoints -> adapter.setDataPoints(dataPoints));
        compositeDisposable.add(disposable);
    }
}
