package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.http.retrofitModels.PockeytTransaction;
import com.pockeyt.cloverpay.models.PockeytTransactionModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class PockeytTransactionViewModel extends ViewModel {
    private static final String TAG = PockeytTransactionViewModel.class.getSimpleName();
    public static final String TRANSACTION_NOT_FOUND = "no_transaction_matching_criteria";
    private MutableLiveData<PockeytTransactionModel> pockeytTransaction;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public LiveData<PockeytTransactionModel> getpockeytTransaction(String orderId) {
        if (pockeytTransaction == null) {
            pockeytTransaction = new MutableLiveData<PockeytTransactionModel>();
            fetchPockeytTransaction(orderId);
        } else if (pockeytTransaction.getValue() != null && !pockeytTransaction.getValue().getOrderId().equals(orderId)) {
            pockeytTransaction = new MutableLiveData<PockeytTransactionModel>();
            fetchPockeytTransaction(orderId);
        }
        return pockeytTransaction;
    }

    public void setPockeytTransaction(PockeytTransactionModel pockeytTransactionModel) {
       if (this.pockeytTransaction == null) {
           this.pockeytTransaction = new MutableLiveData<PockeytTransactionModel>();
       }
       this.pockeytTransaction.setValue(pockeytTransactionModel);
    }

    public void fetchPockeytTransaction(String orderId) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Observable<Response<PockeytTransaction>> pockeytTransactionObservable = apiInterface.doGetPockeytTransaction(orderId);
        Disposable disposable = pockeytTransactionObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResult, this::handleError);
        mCompositeDisposable.add(disposable);
    }

    private void handleResult(Response<PockeytTransaction> pockeytTransactionResponse) {
        if (pockeytTransactionResponse.isSuccessful()) {
            PockeytTransactionModel pockeytTransactionModel = setPockeytTransactionModel(pockeytTransactionResponse.body());
            setPockeytTransaction(pockeytTransactionModel);
        } else {
            try {
                JSONObject errorBody = new JSONObject(pockeytTransactionResponse.errorBody().string());
                if (errorBody.getString("error").equals(TRANSACTION_NOT_FOUND)) {
                    setPockeytTransaction(null);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private PockeytTransactionModel setPockeytTransactionModel(PockeytTransaction pockeytTransaction) {
        return new PockeytTransactionModel(
          pockeytTransaction.getData().getId(),
          pockeytTransaction.getData().getBusinessId(),
          pockeytTransaction.getData().getCustomerId(),
          pockeytTransaction.getData().getTax(),
          pockeytTransaction.getData().getSubTotal(),
          pockeytTransaction.getData().getTotal(),
          pockeytTransaction.getData().getPosTransactionId()
        );
    }


    private void handleError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCompositeDisposable.dispose();
    }
}
