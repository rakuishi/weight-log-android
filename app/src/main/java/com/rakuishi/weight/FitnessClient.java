package com.rakuishi.weight;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import timber.log.Timber;

import static java.text.DateFormat.getDateInstance;

public class FitnessClient implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public interface Callback {
        void onConnectionSuccess();

        void onConnectionFail(Exception e);
    }

    private GoogleApiClient client;
    private FragmentActivity activity;
    private Callback callback;

    /**
     * https://developers.google.com/android/reference/com/google/android/gms/fitness/HistoryApi
     * https://developers.google.com/android/reference/com/google/android/gms/common/Scopes#FITNESS_BODY_READ_WRITE
     */
    public FitnessClient(FragmentActivity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
        this.client = new GoogleApiClient.Builder(activity)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(this)
                .enableAutoManage(activity, this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("onConnected");
        if (callback != null) {
            callback.onConnectionSuccess();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.d("onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (callback != null) {
            callback.onConnectionFail(new IllegalStateException(connectionResult.getErrorMessage()));
        }
    }

    public Observable<List<DataPoint>> find(int amount) {
        return Observable.create(e -> {
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(client, getDataReadRequest(amount)).await(1, TimeUnit.MINUTES);

            if (dataReadResult.getDataSets().size() == 1) {
                e.onNext(reverse(dataReadResult.getDataSets().get(0).getDataPoints()));
                e.onComplete();
            } else {
                e.onError(new IllegalStateException("Failed to find user's weight data-points."));
            }
        });
    }

    private DataReadRequest getDataReadRequest(int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, -1 * amount);
        long startTime = calendar.getTimeInMillis();

        DateFormat dateFormat = getDateInstance();
        Timber.d("start: " + dateFormat.format(startTime) + " ~ end: " + dateFormat.format(endTime));

        return new DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    private List<DataPoint> reverse(@Nullable List<DataPoint> dataPoints) {
        List<DataPoint> reverseDataPoints = new ArrayList<>();

        if (dataPoints != null) {
            for (int i = dataPoints.size() - 1; i >= 0; i--) {
                reverseDataPoints.add(dataPoints.get(i));
            }
        }

        return reverseDataPoints;
    }

    public Completable insert(float weight) {
        return Completable.create(e -> {
            Status status = Fitness.HistoryApi.insertData(client, getInsertDataSet(weight))
                    .await(1, TimeUnit.MINUTES);
            if (status.isSuccess()) {
                e.onComplete();
            } else {
                e.onError(new IllegalStateException(status.getStatusMessage()));
            }
        });
    }

    private DataSet getInsertDataSet(float weight) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(activity)
                .setDataType(DataType.TYPE_WEIGHT)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName(activity.getPackageName() + "_weight")
                .build();

        DataSet dataSet = DataSet.create(dataSource);

        DataPoint dataPoint = dataSet.createDataPoint()
                .setTimestamp(calendar.getTimeInMillis(), TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_WEIGHT).setFloat(weight);

        dataSet.add(dataPoint);

        return dataSet;
    }

    // TODO: An app cannot delete data inserted by other apps.
    public Completable delete(Long millis) {
        return Completable.create(e -> {
            Status status = Fitness.HistoryApi.deleteData(client, getDataDeleteRequest(millis))
                    .await(1, TimeUnit.MINUTES);
            if (status.isSuccess()) {
                e.onComplete();
            } else {
                e.onError(new IllegalStateException(status.getStatusMessage()));
            }
        });
    }

    private DataDeleteRequest getDataDeleteRequest(Long millis) {
        // 指定された時刻周辺の DataPoint を削除する。開始時刻と終了時刻を同一にすることは許可されていない
        return new DataDeleteRequest.Builder()
                .setTimeInterval(millis, millis + 1L, TimeUnit.MILLISECONDS)
                .addDataType(DataType.TYPE_WEIGHT)
                .build();
    }
}
