package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.pockeyt.cloverpay.models.CloverTransactionModel;

public class CloverTransactionViewModel extends ViewModel {
    private static final String TAG = CloverTransactionViewModel.class.getSimpleName();
    private MutableLiveData<CloverTransactionModel> cloverTransaction;

    public LiveData<CloverTransactionModel> getCloverTransaction() {
        if (cloverTransaction == null) {
            cloverTransaction = new MutableLiveData<CloverTransactionModel>();
        }
        return cloverTransaction;
    }

    public void setCloverTransaction(CloverTransactionModel cloverTransaction) {
        if (this.cloverTransaction == null) {
            this.cloverTransaction = new MutableLiveData<CloverTransactionModel>();
        }
        this.cloverTransaction.setValue(cloverTransaction);
    }
}
