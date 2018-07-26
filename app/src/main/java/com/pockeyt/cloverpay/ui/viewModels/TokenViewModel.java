package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.SharedPreferences;

import com.pockeyt.cloverpay.PockeytPay;
import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.models.TokenModel;
import com.pockeyt.cloverpay.utils.TokenHandler;

public class TokenViewModel extends ViewModel {
    private static final String TAG = TokenViewModel.class.getSimpleName();
    private MutableLiveData<TokenModel> token;
    private TokenHandler tokenHandler;

    public LiveData<TokenModel> getToken() {
        if (token == null) {
            token = new MutableLiveData<TokenModel>();
            retrieveTokenFromStorage();

        }
        return token;
    }

    private void retrieveTokenFromStorage() {
        Context context = PockeytPay.getAppContext();
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String tokenValue = sharedPref.getString(context.getString(R.string.token_key), context.getString(R.string.no_token_in_storage_value));
        int tokenExpiry = sharedPref.getInt(context.getString(R.string.expiry_key), 0);

        TokenModel token = new TokenModel(tokenValue, tokenExpiry);
        setToken(token, false);
    }

    public void setToken(TokenModel tokenModel, Boolean shouldSave) {
        if (shouldSave) {
            saveTokenToStorage(tokenModel);
        }
        if (tokenHandler == null) {
            tokenHandler = new TokenHandler();
        }
        tokenHandler.setToken(tokenModel);
        token.setValue(tokenModel);
    }

    private void saveTokenToStorage(TokenModel tokenModel) {
        Context context = PockeytPay.getAppContext();
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(context.getString(R.string.token_key), tokenModel.getValue());
        editor.putInt(context.getString(R.string.expiry_key), tokenModel.getExpiry());
        editor.apply();
    }
}
