package com.pockeyt.cloverpay.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.pockeyt.cloverpay.PockeytPay;
import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.models.TokenModel;

public class TokenHandler {
    private Context mContext;
    private static TokenModel mToken;

    public TokenHandler() {
        mContext = PockeytPay.getAppContext();
    }

    public TokenModel getToken() {
        if (mToken == null) {
            mToken = retrieveTokenFromStorage();
            return mToken;
        }
        return mToken;
    }

    private TokenModel retrieveTokenFromStorage() {
        SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.preference_file_key), mContext.MODE_PRIVATE);
        String tokenValue = sharedPref.getString(mContext.getString(R.string.token_key), mContext.getString(R.string.no_token_in_storage_value));
        int tokenExpiry = sharedPref.getInt(mContext.getString(R.string.expiry_key), 0);

        return new TokenModel(tokenValue, tokenExpiry);
    }

    public void saveToken(TokenModel token) {
        mToken = token;
        SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.preference_file_key), mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(mContext.getString(R.string.token_key), token.getValue());
        editor.putInt(mContext.getString(R.string.expiry_key), token.getExpiry());
        editor.apply();
    }


}
