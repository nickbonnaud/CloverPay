package com.pockeyt.cloverpay.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DisplayHelpers {

    public static int dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics));
    }

    public static float getDpWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels / metrics.density;
    }

    public static int screenWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        return metrics.widthPixels;
    }

    public static int screenHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        return metrics.heightPixels;
    }
}
