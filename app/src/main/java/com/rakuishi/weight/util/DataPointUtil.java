package com.rakuishi.weight.util;

import com.google.android.gms.fitness.data.DataPoint;

/**
 * DataType.TYPE_WEIGHT
 */
public class DataPointUtil {

    public static boolean hasValue(DataPoint dataPoint) {
        return dataPoint.getDataType().getFields().size() == 1;
    }

    public static float getValue(DataPoint dataPoint) {
        return hasValue(dataPoint)
                ? Float.valueOf(dataPoint.getValue(dataPoint.getDataType().getFields().get(0)).toString())
                : 0f;
    }
}
