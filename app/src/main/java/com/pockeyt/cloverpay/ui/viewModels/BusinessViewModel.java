package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.http.retrofitModels.Business;
import com.pockeyt.cloverpay.models.BusinessModel;
import com.pockeyt.cloverpay.models.TokenModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class BusinessViewModel extends ViewModel {
    private static final String TAG = BusinessViewModel.class.getSimpleName();
    private MutableLiveData<BusinessModel> business;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public LiveData<BusinessModel> getBusiness() {
        if (business == null) {
            business = new MutableLiveData<BusinessModel>();
            fetchBusiness();
        }
        return business;
    }


    public void setBusiness(BusinessModel businessModel) {
        if (this.business == null) {
            this.business = new MutableLiveData<BusinessModel>();
        }
        this.business.setValue(businessModel);
    }

    private void fetchBusiness() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Observable<Response<Business>> businessObservable = apiInterface.doGetBusiness();
        Disposable disposable = businessObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResult, this::handleError);
        mCompositeDisposable.add(disposable);
    }

    private void handleResult(Response<Business> businessResponse) {
        if (businessResponse.isSuccessful()) {
            BusinessModel business = setBusinessModel(businessResponse.body());
            setBusiness(business);
        } else {
            setBusiness(null);
            try {
                JSONObject errorBody = new JSONObject(businessResponse.errorBody().string());
                Log.e(TAG, errorBody.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private BusinessModel setBusinessModel(Business business) {
        TokenModel token = new TokenModel(business.getData().getToken().getValue(), business.getData().getToken().getExpiry());
        return new BusinessModel(
                business.getData().getId(),
                business.getData().getSlug(),
                business.getData().getBusinessName(),
                token,
                business.getData().getLogo(),
                business.getData().getConnectedPos()
        );
    }

    private void handleError(Throwable throwable) {
        setBusiness(null);
        throwable.printStackTrace();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCompositeDisposable.dispose();
    }
}
