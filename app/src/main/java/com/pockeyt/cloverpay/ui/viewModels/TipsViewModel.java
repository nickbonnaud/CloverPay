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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class TipsViewModel extends ViewModel {
    private static final String TAG = TipsViewModel.class.getSimpleName();
    private MutableLiveData<List<TipsModel>> transactions;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private List<EmployeeModel> mEmployees;
    private List<EmployeeModel> mSelectedEmployees;

    private MutableLiveData<Date> startDate;
    private MutableLiveData<Date> endDate;
    private MutableLiveData<Boolean> isLoading;

    private MutableLiveData<Integer> tipTotal;
    private MutableLiveData<List<TipsModel>> selectedTransactionsForTips;

    public LiveData<List<TipsModel>> getEmployeeTransactions(List<EmployeeModel> selectedEmployees, List<EmployeeModel> employees) {
        this.mEmployees = employees;
        if (this.transactions == null || selectedEmployeesChange(selectedEmployees)) {
            this.mSelectedEmployees = selectedEmployees;
            this.transactions = new MutableLiveData<List<TipsModel>>();
            fetchTransactions(selectedEmployees);
        }
        return this.transactions;
    }

    public void paramsChangedSearchTransactions(List<EmployeeModel> selectedEmployees, List<EmployeeModel> employees) {
        this.mEmployees = employees;
        this.mSelectedEmployees = selectedEmployees;
        fetchTransactions(selectedEmployees);
    }


    private boolean selectedEmployeesChange(List<EmployeeModel> selectedEmployees) {
        if (mSelectedEmployees == null && selectedEmployees == null) {
            return false;
        } else if (mSelectedEmployees == null) {
            return true;
        } else if (selectedEmployees == null) {
            return true;
        } else {
            if (mSelectedEmployees.size() != selectedEmployees.size()) {
                return true;
            } else {
                for (int i = 0; i < mSelectedEmployees.size(); i++) {
                    if (!mSelectedEmployees.get(i).getCloverId().equals(selectedEmployees.get(i).getCloverId())) {
                        return true;
                    }
                }
                return false;
            }
        }
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

    private void fetchTransactions(List<EmployeeModel> selectedEmployees) {
        setIsLoading(true);
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Observable<Response<TipsList>> transactionsObservable;
        transactionsObservable = apiInterface.doGetTips(formatQueryParamsMap(selectedEmployees), formatQueryParamsList(selectedEmployees));
        Disposable disposable = transactionsObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResult, this::handleError);
        mCompositeDisposable.add(disposable);
    }

    private Map<String, String> formatQueryParamsMap(List<EmployeeModel> selectedEmployees) {
        Map<String, String> queryParams = new HashMap<>();
        if (startDate.getValue() != null && endDate.getValue() != null) {
            queryParams.put("startTime", formatUrlDate(startDate.getValue()));
            queryParams.put("endTime", formatUrlDate(endDate.getValue()));
        }

        if (selectedEmployees == null || selectedEmployees.size() == 0) {
            queryParams.put("allTips", "1");
        }

        if (queryParams.size() == 0) {
            queryParams.put("default", "1");
        }
        return queryParams;
    }

    private List<String> formatQueryParamsList(List<EmployeeModel> selectedEmployees) {
        List<String> employeeTips = new ArrayList<>();
        if (selectedEmployees != null && selectedEmployees.size() > 0) {
            for (EmployeeModel employee : selectedEmployees) {
                employeeTips.add(employee.getCloverId());
            }
        }
        return employeeTips.size() == 0 ? null : employeeTips;
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
        throwable.printStackTrace();
    }

    private void formatTransactionsList(TipsList tipsList) {
        List<TipsModel> transactionsListHolder = new ArrayList<TipsModel>();
        for (TipsList.Datum tip : tipsList.getData()) {

            String name = getEmployeeName(tip.getEmployeeId());
            TipsModel tipsModel = new TipsModel(
                    tip.getDate(),
                    name,
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

    private String getEmployeeName(String employeeId) {
        for (EmployeeModel employee : mEmployees) {
            if (employee.getCloverId().equals(employeeId)) {
                return employee.getName();
            }
        }
        return "Unknown";
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
