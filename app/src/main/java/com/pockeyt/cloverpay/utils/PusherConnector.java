package com.pockeyt.cloverpay.utils;

import android.content.Context;

import com.pockeyt.cloverpay.PockeytPay;
import com.pockeyt.cloverpay.R;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.util.HttpAuthorizer;

import java.util.HashMap;

public class PusherConnector {
    private Context mContext;
    private String mBusinessSlug;
    private String mBusinessToken;
    private Channel mPusherChannel;
    private boolean mIsPusherConnected = false;
    public String mPusherEvent = "App\\Events\\UpdateConnectedApps";

    public PusherConnector(String businessSlug, String businessToken) {
        mBusinessSlug = businessSlug;
        mBusinessToken = businessToken;
    }

    public void connectToPusher() {
        mContext = PockeytPay.getAppContext();
        PusherOptions options = new PusherOptions();

        HttpAuthorizer authorizer = new HttpAuthorizer(mContext.getString(R.string.base_url) + "/pusher/" + mBusinessSlug);
        authorizer.setHeaders(getMapAuthHeaders());

        options.setCluster("mt1");
        options.setAuthorizer(authorizer);
        Pusher pusher = new Pusher("f4976d40a137b96b52ea", options);
        pusher.connect();

        String channelId = "private-update." + mBusinessSlug;
        mPusherChannel = pusher.subscribePrivate(channelId);
        mIsPusherConnected = true;
    }

    private HashMap<String,String> getMapAuthHeaders() {
        try {
            HashMap<String, String> authHeader = new HashMap<>();
            authHeader.put("Authorization", "Bearer " + mBusinessToken);
            authHeader.put("Accept", "application/json");
            return authHeader;
        } catch (Exception e) {
            return null;
        }
    }


    public Channel getPusherChannel() {
        return mPusherChannel;
    }

    public boolean getIsPusherConnected() {
        return mIsPusherConnected;
    }

    public String getPusherEvent() {
        return mPusherEvent;
    }
}
