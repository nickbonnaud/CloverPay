package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.pockeyt.cloverpay.handlers.BusinessHandler;
import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.http.retrofitModels.Business;
import com.pockeyt.cloverpay.models.BusinessModel;
import com.pockeyt.cloverpay.models.TokenModel;
import com.pockeyt.cloverpay.utils.TokenHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class MainActivityViewModel extends ViewModel {
    private MutableLiveData<BusinessModel> business;
    private MutableLiveData<String> error;
    private boolean mShouldFetchBusiness;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();


    public MainActivityViewModel(boolean shouldFetch) {
        mShouldFetchBusiness = shouldFetch;
    }

    public LiveData<BusinessModel> getBusiness() {
        if (business == null) {
            business = new MutableLiveData<BusinessModel>();
            if (mShouldFetchBusiness) {
                mShouldFetchBusiness = false;
                loadBusiness();
            } else {
                BusinessModel savedBusiness = BusinessHandler.getBusiness();
                setBusiness(savedBusiness);
            }
        }
        return business;
    }

    public LiveData<String> getError() {
        if (error == null) {
            error = new MutableLiveData<String>();
        }
        return error;
    }

    private void loadBusiness() {
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
            try {
                JSONObject errorBody = new JSONObject(businessResponse.errorBody().string());
                setError(errorBody.getString("error"));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleError(Throwable throwable) {
        setError(throwable.getMessage());
    }


    private BusinessModel setBusinessModel(Business business) {
        TokenModel token = setAndSaveToken(business);
        BusinessModel businessModel = new BusinessModel(
                business.getData().getId(),
                business.getData().getSlug(),
                business.getData().getBusinessName(),
                token,
                business.getData().getLogo()
        );
        BusinessHandler.setBusiness(businessModel);
        return businessModel;
    }

    private TokenModel setAndSaveToken(Business business) {
        TokenModel token = new TokenModel(business.getData().getToken().getValue(), business.getData().getToken().getExpiry());
        TokenHandler tokenHandler = new TokenHandler();
        tokenHandler.saveToken(token);
        return token;
    }

    private void setError(String error) {
        this.error.setValue(error);
    }

    private void setBusiness(BusinessModel business) {
        this.business.setValue(business);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCompositeDisposable.dispose();
    }
}
