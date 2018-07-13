package com.pockeyt.cloverpay.utils;

import android.accounts.Account;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.IInterface;
import android.util.Log;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.CloverAuth;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.tender.Tender;
import com.clover.sdk.v1.tender.TenderConnector;
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.InventoryConnector;
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
        getTenders();
    }

    private void getTenders() {
        Observable<List<Tender>> observable = Observable.fromCallable(()
                -> mTenderConnector.getTenders());
        Disposable disposable = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResultGetTenders, this::handleError);
        mCompositeDisposable.add(disposable);
    }

    public void getAuthToken() {
        Observable<CloverAuth.AuthResult> observable = Observable.fromCallable(()
            -> CloverAuth.authenticate(mContext, mAccount));
        Disposable disposable = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResultGetToken, this::handleError);
        mCompositeDisposable.add(disposable);
    }


    private void handleError(Throwable throwable) {
        throwable.printStackTrace();
        Log.e(TAG, throwable.getMessage());
    }

    private void handleResultGetToken(CloverAuth.AuthResult authResult) {
        if (authResult.authToken != null && authResult.merchantId != null) {
            postTokenData(authResult.authToken, authResult.merchantId);
        }
    }


    private void handleResultGetTenders(List<Tender> tenders) {
        boolean tenderExists = false;
        if (tenders != null) {
            for (Tender tender : tenders) {
                if (mContext.getString(R.string.tender_name).equals(tender.getLabel())) {
                    tenderExists = true;
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
        Toast.makeText(mContext, "Pockeyt Pay Enabled!", Toast.LENGTH_LONG).show();
    }

    public CloverTransactionModel setCloverTransaction(Intent intent) {
        return new CloverTransactionModel(
                intent.getLongExtra(Intents.EXTRA_AMOUNT, 0),
                intent.getLongExtra(Intents.EXTRA_TAX_AMOUNT, 0),
                intent.getStringExtra(Intents.EXTRA_ORDER_ID),
                intent.getStringExtra(Intents.EXTRA_MERCHANT_ID)
        );
    }

    private void postTokenData(String authToken, String merchantId) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Observable<Response<Business>> postTokenObservable = apiInterface.doRequestSaveAuthToken("clover", authToken, merchantId, "PATCH");
        Disposable disposable = postTokenObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResultSaveToken, this::handleError);
        mCompositeDisposable.add(disposable);
    }

    private void handleResultSaveToken(Response<Business> businessResponse) {
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

    public void destroy() {
        if (mTenderConnector != null) {
            mTenderConnector.disconnect();
            mTenderConnector = null;
        }
        mCompositeDisposable.dispose();
    }
}
