package com.pockeyt.cloverpay.utils;

import android.accounts.Account;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.CloverAuth;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.tender.Tender;
import com.clover.sdk.v1.tender.TenderConnector;
import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.http.retrofitModels.Business;
import com.pockeyt.cloverpay.models.BusinessModel;
import com.pockeyt.cloverpay.models.CloverTransactionModel;
import com.pockeyt.cloverpay.ui.activities.MainActivity;
import com.pockeyt.cloverpay.ui.viewModels.BusinessViewModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class CloverTenderConnecter {
    private static final String TAG = CloverTenderConnecter.class.toString();
    private TenderConnector mTenderConnector;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Context mContext;
    private Account mAccount;

    public CloverTenderConnecter(Context context) {
        this.mContext = context;
        this.mAccount = CloverAccount.getAccount(context);
        this.mTenderConnector = new TenderConnector(context, mAccount, null);
    }

    public void init() {
        mTenderConnector.connect();
    }

    public void getAccountData() {
        getTenders();
        getAuthToken();
    }

    private void getTenders() {
        Observable<List<Tender>> observable = Observable.fromCallable(()
                -> mTenderConnector.getTenders());
        Disposable disposable =  observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResultGetTenders, this::handleError);
        mCompositeDisposable.add(disposable);
    }

    private void getAuthToken() {
        Observable<CloverAuth.AuthResult> observable = Observable.fromCallable(()
                -> CloverAuth.authenticate(mContext, mAccount));
        Disposable disposable = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResultGetToken, this::handleError);
        mCompositeDisposable.add(disposable);
    }

    private void handleResultGetTenders(List<Tender> tenders) {
        boolean tenderExists = false;
        if (tenders != null) {
            for (Tender tender : tenders) {
                if (mContext.getString(R.string.tender_name).equals(tender.getLabel())) {
                    tenderExists = true;
                    postTenderData(tender.getId());
                    break;
                }
            }
        }
        if (!tenderExists) {
            createTender();
        }
    }

    private void createTender() {
        Observable<Tender> observable = Observable.fromCallable(()
                -> mTenderConnector.checkAndCreateTender(mContext.getString(R.string.tender_name), mContext.getPackageName(), true, false));
        Disposable disposable = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResultCreateTender, this::handleError);
        mCompositeDisposable.add(disposable);
    }


    private void handleResultCreateTender(Tender tender) {
        Log.d(TAG, tender.getLabel());
        String tenderId = tender.getId();
        postTenderData(tenderId);
    }

    private void postTenderData(String tenderId) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Observable<Response<Business>> postTenderObservable = apiInterface.doSendTenderId(tenderId, "PATCH");
        Disposable disposable = postTenderObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResultUpdateBusiness, this::handleError);
        mCompositeDisposable.add(disposable);
    }


    private void handleResultGetToken(CloverAuth.AuthResult authResult) {
        if (authResult.authToken != null && authResult.merchantId != null) {
            postTokenData(authResult.authToken, authResult.merchantId);
        }
    }

    private void postTokenData(String authToken, String merchantId) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Observable<Response<Business>> postTokenObservable = apiInterface.doRequestSaveAuthToken("clover", authToken, merchantId, "PATCH");
        Disposable disposable = postTokenObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResultUpdateBusiness, this::handleError);
        mCompositeDisposable.add(disposable);
    }

    private void handleResultUpdateBusiness(Response<Business> businessResponse) {
        if (businessResponse.isSuccessful()) {
            updateBusiness(businessResponse.body().getData());
        }
    }

    private void updateBusiness(Business.Data data) {
        if (data.getConnectedPos() != null && data.getConnectedPos().equals("clover")) {
            MainActivity mainActivity = (MainActivity) mContext;

            BusinessViewModel businessViewModel = ViewModelProviders.of(mainActivity).get(BusinessViewModel.class);
            BusinessModel business = businessViewModel.getBusiness().getValue();
            business.setConnectedPos(data.getConnectedPos());
            businessViewModel.setBusiness(business);
        }
    }

    public CloverTransactionModel setCloverTransaction(Intent intent) {
        Log.d(TAG,"Error: " + intent.getStringExtra(Intents.EXTRA_EMPLOYEE_ID));
        return new CloverTransactionModel(
                intent.getLongExtra(Intents.EXTRA_AMOUNT, 0),
                intent.getLongExtra(Intents.EXTRA_TAX_AMOUNT, 0),
                intent.getStringExtra(Intents.EXTRA_ORDER_ID),
                intent.getStringExtra(Intents.EXTRA_MERCHANT_ID),
                intent.getStringExtra(Intents.EXTRA_EMPLOYEE_ID)
        );
    }

    private void handleError(Throwable throwable) {
        throwable.printStackTrace();
        Log.e(TAG, throwable.getMessage());
    }

    public void destroy() {
        if (mTenderConnector != null) {
            mTenderConnector.disconnect();
            mTenderConnector = null;
        }
        mCompositeDisposable.dispose();
    }
}
