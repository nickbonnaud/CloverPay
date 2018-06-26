package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.pockeyt.cloverpay.handlers.CustomerHandler;
import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.http.retrofitModels.CustomerList;
import com.pockeyt.cloverpay.models.CustomerModel;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class CustomersViewModel extends ViewModel {
    private static final String TAG = CustomersViewModel.class.getSimpleName();
    private MutableLiveData<CustomerModel[]> customers;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public LiveData<CustomerModel[]> getCustomers() {
        if (customers == null) {
            customers = new MutableLiveData<CustomerModel[]>();
            loadCustomers();
        }
        return customers;
    }

    private void loadCustomers() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Observable<Response<CustomerList>> customerObservable = apiInterface.doGetCustomers();

        Disposable disposable = customerObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResult, this::handleError);

        mCompositeDisposable.add(disposable);
    }

    private void handleError(Throwable throwable) {
        Log.e(TAG, "ERROR: " + throwable.getMessage());
    }

    private void handleResult(Response<CustomerList> customerListResponse) throws IOException {
        if (customerListResponse.isSuccessful()) {
            CustomerList customerList = customerListResponse.body();
            CustomerModel[] customerModels = CustomerHandler.setCustomers(customerList.getData());
            setCustomers(customerModels);
        } else {
            Log.d(TAG, "BODY: " + customerListResponse.errorBody().string());
        }
    }

    public void setCustomers(CustomerModel[] customerModels) {
        customers.setValue(customerModels);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCompositeDisposable.dispose();
    }
}
