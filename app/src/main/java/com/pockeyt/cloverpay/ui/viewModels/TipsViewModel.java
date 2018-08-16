package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.http.retrofitModels.TipsList;
import com.pockeyt.cloverpay.models.EmployeeModel;
import com.pockeyt.cloverpay.models.TipsModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class TipsViewModel extends ViewModel {
    private static final String TAG = TipsViewModel.class.getSimpleName();
    private MutableLiveData<List<TipsModel>> transactions;
    private EmployeeModel mEmployee;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private MutableLiveData<Date> startDate;
    private MutableLiveData<Date> endDate;
    private MutableLiveData<Boolean> isLoading;

    private MutableLiveData<Integer> tipTotal;
    private MutableLiveData<List<TipsModel>> selectedTransactionsForTips;

    public LiveData<List<TipsModel>> getEmployeeTransactions(boolean isManager, EmployeeModel employee) {
        if (this.transactions == null || this.mEmployee == null || !this.mEmployee.getCloverId().equals(employee.getCloverId())) {
            this.mEmployee = employee;
            this.transactions = new MutableLiveData<List<TipsModel>>();
            fetchTransactions(isManager);
        }
        return this.transactions;
    }

    public void getEmployeeTransactionsWithDates(boolean isManager, EmployeeModel employee) {
        this.mEmployee = employee;
        fetchTransactions(isManager);
    }

    public void getEmployeeTransactionsDefault(boolean isManager, EmployeeModel employee) {
        this.mEmployee = employee;
        fetchTransactions(isManager);
    }

    public void setIsLoading(Boolean isLoading) {
        this.isLoading.setValue(isLoading);
    }

    public LiveData<Boolean> getIsLoading() {
        if (this.isLoading == null) {
            this.isLoading = new MutableLiveData<Boolean>();
        }
        return this.isLoading;
    }

    public void setStartDate(Date date) {
        if (this.startDate == null) {
            this.startDate = new MutableLiveData<Date>();
        }
        this.startDate.setValue(date);
    }

    public LiveData<Date> getStartDate() {
        if (this.startDate == null) {
            this.startDate = new MutableLiveData<Date>();
        }
        return this.startDate;
    }

    public void setEndDate(Date date) {
        if (this.endDate == null) {
            this.endDate = new MutableLiveData<Date>();
        }
        this.endDate.setValue(date);
    }

    public LiveData<Date> getEndDate() {
        if (this.endDate == null) {
            this.endDate = new MutableLiveData<Date>();
        }
        return this.endDate;
    }

    private void fetchTransactions(boolean isManager) {
        setIsLoading(true);
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Observable<Response<TipsList>> transactionsObservable;
        if (isManager) {
            if (startDate.getValue() != null && endDate.getValue() != null) {
                transactionsObservable = apiInterface.doGetTipsAllWithDates(1, formatUrlDate(startDate.getValue()), formatUrlDate(endDate.getValue()));
            } else {
                Log.d(TAG, "Inside null");
                transactionsObservable = apiInterface.doGetTipsAll(1);
            }

        } else {
            if (startDate.getValue() != null && endDate.getValue() != null) {
                transactionsObservable = apiInterface.doGetTipsEmployeeWithDates(mEmployee.getCloverId(), formatUrlDate(startDate.getValue()), formatUrlDate(endDate.getValue()));
            } else {
                transactionsObservable = apiInterface.doGetTipsEmployee(mEmployee.getCloverId());
            }
        }
        Disposable disposable = transactionsObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResult, this::handleError);
        mCompositeDisposable.add(disposable);
    }

    private void handleResult(Response<TipsList> tipsListResponse) {
        if (tipsListResponse.isSuccessful()) {
            formatTransactionsList(tipsListResponse.body());
        } else {
            Log.e(TAG, tipsListResponse.errorBody().toString());
        }
        setIsLoading(false);
    }

    private void handleError(Throwable throwable) {
        setIsLoading(false);
        Log.e(TAG, "ERROR: " + throwable.getMessage());
    }

    private void formatTransactionsList(TipsList tipsList) {
        List<TipsModel> transactionsListHolder = new ArrayList<TipsModel>();
        for (TipsList.Datum tip : tipsList.getData()) {
            TipsModel tipsModel = new TipsModel(
                    tip.getDate(), mEmployee.getName(),
                    tip.getEmployeeId(),
                    tip.getTransactionId(),
                    formatCurrency(tip.getTotal()),
                    formatCurrency(tip.getTip()),
                    tip.getTip());

            transactionsListHolder.add(tipsModel);
        }
        if (startDate.getValue() != null && endDate.getValue() != null) {
            setSelectedTransactionsForTips(transactionsListHolder);
        }
        setEmployeeTransactions(transactionsListHolder);
    }

    private void setEmployeeTransactions(List<TipsModel> transactionsListHolder) {
        this.transactions.setValue(transactionsListHolder);
    }

    public void setSelectedTransactionsForTips(List<TipsModel> transactionsForTips) {
        if (this.selectedTransactionsForTips == null) {
            this.selectedTransactionsForTips = new MutableLiveData<List<TipsModel>>();
        }
        this.selectedTransactionsForTips.setValue(transactionsForTips);
        setTipTotal(sumTipsOfTransactions(transactionsForTips));
    }

    public void setTipTotal(Integer tipTotal) {
        if (this.tipTotal == null) {
            this.tipTotal = new MutableLiveData<Integer>();
        }
        this.tipTotal.setValue(tipTotal);
    }

    public LiveData<Integer> getTipTotal() {
        if (this.tipTotal == null) {
            this.tipTotal = new MutableLiveData<Integer>();
        }
        return tipTotal;
    }


    public boolean addRemoveTransaction(Integer indexOfTransaction) {
        TipsModel transaction = this.transactions.getValue().get(indexOfTransaction);
        if (this.selectedTransactionsForTips != null && this.selectedTransactionsForTips.getValue() != null) {
            int index = this.selectedTransactionsForTips.getValue().indexOf(transaction);
             if (index == -1) {
                 addToSelectedTransactionsForTips(transaction);
                 return true;
             } else {
                 subtractFromSelectedTransactionForTips(index);
                 return false;
             }
        } else {
            this.selectedTransactionsForTips = new MutableLiveData<List<TipsModel>>();
            List<TipsModel> tipsList = new ArrayList<TipsModel>();
            tipsList.add(transaction);
            setSelectedTransactionsForTips(tipsList);
            return true;
        }
    }

    private void addToSelectedTransactionsForTips(TipsModel transaction) {
        List<TipsModel> selectedTransactions  = this.selectedTransactionsForTips.getValue();
        selectedTransactions.add(transaction);
        setSelectedTransactionsForTips(selectedTransactions);
    }

    private void subtractFromSelectedTransactionForTips(int index) {
        List<TipsModel> selectedTransactions  = this.selectedTransactionsForTips.getValue();
        selectedTransactions.remove(index);
        setSelectedTransactionsForTips(selectedTransactions);
    }








    private Integer sumTipsOfTransactions(List<TipsModel> transactionsForTips) {
        Integer tipTotal = 0;
        for (TipsModel transaction : transactionsForTips) {
           tipTotal = tipTotal + transaction.getTipAmount();
        }
        return tipTotal;
    }

    private String formatCurrency(int amount) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        return n.format(amount / 100.0);
    }

    private String formatUrlDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault())
                .format(date);
    }
}
