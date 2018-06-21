package com.pockeyt.cloverpay.utils;

import android.content.Context;
import android.util.Log;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.tender.Tender;
import com.clover.sdk.v1.tender.TenderConnector;
import com.pockeyt.cloverpay.R;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CloverTenderConnecter {
    private static final String TAG = CloverTenderConnecter.class.toString();
    private TenderConnector mTenderConnector;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Context mContext;

    public CloverTenderConnecter(Context context) {
        this.mContext = context;
        this.mTenderConnector = new TenderConnector(context, CloverAccount.getAccount(context), null);
    }

    public void init() {
        mTenderConnector.connect();
        Observable<List<Tender>> observable = Observable.fromCallable(()
                -> mTenderConnector.getTenders());
        Disposable disposable = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResultGetTenders, this::handleError);
        mCompositeDisposable.add(disposable);
    }

    private void handleError(Throwable throwable) {
        throwable.printStackTrace();
        Log.e(TAG, throwable.getMessage());
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
        // Show success toast
    }

    public void destroy() {
        if (mTenderConnector != null) {
            mTenderConnector.disconnect();
            mTenderConnector = null;
        }
        mCompositeDisposable.dispose();
    }
}
