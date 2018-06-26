package com.pockeyt.cloverpay.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.pockeyt.cloverpay.PockeytPay;
import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.models.TokenModel;

public class TokenHandler {
    private static TokenModel mToken;

    public TokenModel getToken() {
        return mToken;
    }


    public void setToken(TokenModel token) {
        mToken = token;
    }

}
