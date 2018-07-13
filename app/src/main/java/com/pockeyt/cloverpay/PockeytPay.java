package com.pockeyt.cloverpay;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

public class PockeytPay extends MultiDexApplication {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        PockeytPay.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return PockeytPay.context;
    }

}
