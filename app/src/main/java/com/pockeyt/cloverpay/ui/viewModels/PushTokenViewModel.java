package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class PushTokenViewModel extends ViewModel {
    private MutableLiveData<String> token;

    public LiveData<String> getPushToken() {
        if (token == null) {
            token = new MutableLiveData<String>();
        }
        return token;
    }

    public void setPushToken(String pushToken) {
        if (token == null) {
            token = new MutableLiveData<String>();
        }
        token.setValue(pushToken);
    }
}
