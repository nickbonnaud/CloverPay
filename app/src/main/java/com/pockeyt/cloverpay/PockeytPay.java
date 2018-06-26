package com.pockeyt.cloverpay;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.pockeyt.cloverpay.handlers.BusinessHandler;
import com.pockeyt.cloverpay.models.BusinessModel;
import com.pockeyt.cloverpay.models.TokenModel;
import com.pockeyt.cloverpay.utils.TokenHandler;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.util.HttpAuthorizer;

import java.util.HashMap;

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
