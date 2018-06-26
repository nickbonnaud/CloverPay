package com.pockeyt.cloverpay.utils;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;

import com.pockeyt.cloverpay.PockeytPay;
import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.models.BusinessModel;
import com.pockeyt.cloverpay.models.TokenModel;
import com.pockeyt.cloverpay.ui.activities.MainActivity;
import com.pockeyt.cloverpay.ui.viewModels.BusinessViewModel;
import com.pockeyt.cloverpay.ui.viewModels.TokenViewModel;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.util.HttpAuthorizer;

import java.util.HashMap;

public class PusherConnector {
    private Context mContext;
    private MainActivity mMainActivity;
    private Channel mPusherChannel;
    private boolean mIsPusherConnected = false;
    public String mPusherEvent = "App\\Events\\UpdateConnectedApps";

    public PusherConnector(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
    }

    public void connectToPusher() {
        mContext = PockeytPay.getAppContext();

        PusherOptions options = new PusherOptions();
        BusinessViewModel businessViewModel = ViewModelProviders.of(mMainActivity).get(BusinessViewModel.class);
        BusinessModel business = businessViewModel.getBusiness().getValue();

        HttpAuthorizer authorizer = new HttpAuthorizer(mContext.getString(R.string.base_url) + "/pusher/" + business.getSlug());
        authorizer.setHeaders(getMapAuthHeaders());

        options.setCluster("mt1");
        options.setAuthorizer(authorizer);
        Pusher pusher = new Pusher("f4976d40a137b96b52ea", options);
        pusher.connect();

        String channelId = "private-update." + business.getSlug();
        mPusherChannel = pusher.subscribePrivate(channelId);
        mIsPusherConnected = true;
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
        TokenViewModel tokenViewModel = ViewModelProviders.of(mMainActivity).get(TokenViewModel.class);
        TokenModel token = tokenViewModel.getToken().getValue();
        return token.getValue();
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
