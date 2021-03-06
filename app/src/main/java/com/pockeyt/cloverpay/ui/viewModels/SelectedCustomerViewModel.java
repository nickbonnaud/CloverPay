package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.pockeyt.cloverpay.models.CustomerModel;

public class SelectedCustomerViewModel extends ViewModel {
    private static final String TAG = SelectedCustomerViewModel.class.getSimpleName();
    private MutableLiveData<CustomerModel> selectedCustomer;
    private MutableLiveData<Boolean> isLoadingDeal;
    private MutableLiveData<Boolean> isLoadingLoyalty;

    public LiveData<CustomerModel> getCustomer() {
        if (selectedCustomer == null) {
            selectedCustomer = new MutableLiveData<CustomerModel>();
        }
        return selectedCustomer;
    }

    public void setCustomer(CustomerModel customer) {
        if (customer != null) {
            if (selectedCustomer.getValue() != null && selectedCustomer.getValue().getId() == customer.getId()) {
                isLoadingDeal = new MutableLiveData<Boolean>();
                isLoadingLoyalty = new MutableLiveData<Boolean>();
            }
        } else {
            isLoadingDeal = new MutableLiveData<Boolean>();
            isLoadingLoyalty = new MutableLiveData<Boolean>();
        }
        Log.d(TAG, "before");
        selectedCustomer.setValue(customer);
        Log.d(TAG, "after");
    }

    public LiveData<Boolean> getIsLoadingDeal() {
        if (isLoadingDeal == null) {
            isLoadingDeal = new MutableLiveData<Boolean>();
            setIsLoadingDeal(false);
        }
        return isLoadingDeal;
    }

    public void setIsLoadingDeal(boolean isLoadingDeal) {
        this.isLoadingDeal.setValue(isLoadingDeal);
    }

    public LiveData<Boolean> getIsLoadingLoyalty() {
        if (isLoadingLoyalty == null) {
            isLoadingLoyalty = new MutableLiveData<Boolean>();
            setIsLoadingLoyalty(false);
        }
        return isLoadingLoyalty;
    }

    public void setIsLoadingLoyalty(boolean isLoadingLoyalty) {
        this.isLoadingLoyalty.setValue(isLoadingLoyalty);
    }

    public void resetSelectedCustomer() {
        selectedCustomer = new MutableLiveData<CustomerModel>();
        isLoadingDeal = new MutableLiveData<Boolean>();
        isLoadingLoyalty = new MutableLiveData<Boolean>();
    }
}
