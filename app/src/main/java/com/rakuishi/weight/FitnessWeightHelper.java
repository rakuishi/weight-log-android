package com.rakuishi.weight;

import android.content.Context;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import timber.log.Timber;

import static java.text.DateFormat.getDateInstance;

public class FitnessWeightHelper {

    /**
     * https://developers.google.com/android/reference/com/google/android/gms/fitness/HistoryApi
     * https://developers.google.com/android/reference/com/google/android/gms/common/Scopes#FITNESS_BODY_READ_WRITE
     */
    public static GoogleApiClient buildClient(
            Context context, GoogleApiClient.ConnectionCallbacks connectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener) {

        return new GoogleApiClient.Builder(context)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .build();
    }

    public static Observable<List<DataPoint>> loadDataPoints(GoogleApiClient client, int amount) {
        return Observable.create(e -> {
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(client, getDataReadRequest(amount)).await(1, TimeUnit.MINUTES);

            if (dataReadResult.getDataSets().size() == 1) {
                e.onNext(dataReadResult.getDataSets().get(0).getDataPoints());
                e.onComplete();
            } else {
                e.onError(new IllegalStateException("Your weight data are something wrong."));
            }
        });
    }

    private static DataReadRequest getDataReadRequest(int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.WEEK_OF_YEAR, -1 * amount);
        long startTime = calendar.getTimeInMillis();

        DateFormat dateFormat = getDateInstance();
        Timber.d("Range Start: " + dateFormat.format(startTime));
        Timber.d("Range End: " + dateFormat.format(endTime));

        return new DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }
}
