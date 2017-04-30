package com.rakuishi.weight.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class DensityUtil {

    public static float dp2Px(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }

    public static float px2Dp(Context context, int px) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / metrics.density;
    }
}
