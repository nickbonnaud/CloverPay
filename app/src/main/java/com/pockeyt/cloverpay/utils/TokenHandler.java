package com.pockeyt.cloverpay.utils;

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
