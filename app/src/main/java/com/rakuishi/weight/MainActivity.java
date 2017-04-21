package com.rakuishi.weight;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient client = null;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        compositeDisposable = new CompositeDisposable();

        buildFitnessClient();
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
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!client.isConnecting() && !client.isConnected()) {
                    client.connect();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    // region GoogleApiClient.ConnectionCallbacks

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("onConnected");
        loadWeightData();
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

    // region private methods

    private void buildFitnessClient() {
        // https://developers.google.com/android/reference/com/google/android/gms/fitness/HistoryApi
        // https://developers.google.com/android/reference/com/google/android/gms/common/Scopes#FITNESS_BODY_READ_WRITE

        client = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void loadWeightData() {
        Observable<DataReadResult> observable = Observable.create(e -> {
            e.onNext(Fitness.HistoryApi.readData(client, queryWeightData()).await(1, TimeUnit.MINUTES));
            e.onComplete();
        });
        Disposable disposable = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(dataReadResult -> {
                    if (dataReadResult != null) {
                        DateFormat dateFormat = getDateInstance();
                        for (DataSet dataSet : dataReadResult.getDataSets()) {
                            for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                Timber.d("Data Point:");
                                Timber.d("\tType: " + dataPoint.getDataType().getName());
                                Timber.d("\tStart: " + dateFormat.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS)));
                                Timber.d("\tEnd: " + dateFormat.format(dataPoint.getEndTime(TimeUnit.MILLISECONDS)));
                                for (Field field : dataPoint.getDataType().getFields()) {
                                    Timber.d("\tFiled: " + field.getName() + ", Value: " + dataPoint.getValue(field));
                                }
                            }
                        }
                    }
                });
        compositeDisposable.add(disposable);
    }

    private DataReadRequest queryWeightData() {
        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.setTime(now);
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.WEEK_OF_YEAR, -4);
        long startTime = calendar.getTimeInMillis();

        DateFormat dateFormat = getDateInstance();
        Timber.d("Range Start: " + dateFormat.format(startTime));
        Timber.d("Range End: " + dateFormat.format(endTime));

        DataReadRequest request = new DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        return request;
    }

    // endregion
}
