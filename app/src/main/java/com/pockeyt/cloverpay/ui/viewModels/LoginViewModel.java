package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class LoginViewModel extends ViewModel {
    private MutableLiveData<String> email;
    private MutableLiveData<String> password;

    public LiveData<String> getEmail() {
        if (email == null) {
            email = new MutableLiveData<String>();
        }
        return email;
    }

    public LiveData<String> getPassword() {
        if (password == null) {
            password = new MutableLiveData<String>();
        }
        return password;
    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }

    public void setPassword(String password) {
        this.password.setValue(password);
    }
}
