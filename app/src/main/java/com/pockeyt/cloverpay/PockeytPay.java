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

    private Boolean isPusherConnected = false;
    private Channel pusherChannel;
    public String pusherEvent = "App\\Events\\UpdateConnectedApps";

    @Override
    public void onCreate() {
        super.onCreate();
        PockeytPay.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return PockeytPay.context;
    }

    public void connectToPusher() {
        BusinessModel business = BusinessHandler.getBusiness();
        PusherOptions options = new PusherOptions();

        HttpAuthorizer authorizer = new HttpAuthorizer(getString(R.string.base_url) + "/pusher/" + business.getSlug());
        authorizer.setHeaders(getMapAuthHeaders());

        options.setCluster("mt1");
        options.setAuthorizer(authorizer);
        Pusher pusher = new Pusher("f4976d40a137b96b52ea", options);
        pusher.connect();

        String channelId = "private-update." + BusinessHandler.getBusiness().getSlug();
        this.pusherChannel = pusher.subscribePrivate(channelId);
        isPusherConnected = true;
    }



    private HashMap<String,String> getMapAuthHeaders() {
        try {
            HashMap<String, String> authHeader = new HashMap<>();
            authHeader.put("Authorization", "Bearer " + getToken());
            authHeader.put("Accept", "application/json");
            return authHeader;
        } catch (Exception e) {
            return null;
        }
    }

    private String getToken() {
        TokenHandler tokenHandler = new TokenHandler();
        TokenModel token = tokenHandler.getToken();
        return token.getValue();
    }

    public Channel getPusherChannel() {
        return pusherChannel;
    }

    public Boolean getPusherConnected() {
        return isPusherConnected;
    }

    public String getPusherEvent() {
        return pusherEvent;
    }
}
