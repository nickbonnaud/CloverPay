package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.clover.sdk.v3.employees.Employee;
import com.pockeyt.cloverpay.handlers.EmployeeHandler;
import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.http.retrofitModels.EmployeeList;
import com.pockeyt.cloverpay.models.EmployeeModel;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class EmployeesViewModel extends ViewModel {
    private static final String TAG = EmployeesViewModel.class.getSimpleName();
    private MutableLiveData<List<EmployeeModel>> employees;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();


    public LiveData<List<EmployeeModel>> getEmployees() {
        if (this.employees == null) {
            this.employees = new MutableLiveData<List<EmployeeModel>>();
            fetchEmployees();
        }
        return this.employees;
    }

    private void fetchEmployees() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Observable<Response<EmployeeList>> employeeObservable = apiInterface.doGetEmployees();

        Disposable disposable = employeeObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResult, this::handleError);
        mCompositeDisposable.add(disposable);
    }

    private void handleResult(Response<EmployeeList> employeeListResponse) throws IOException {
        if (employeeListResponse.isSuccessful()) {
            EmployeeList employeeList = employeeListResponse.body();
            EmployeeHandler employeeHandler = new EmployeeHandler();



            setEmployees(employeeHandler.setEmployees(employeeList.getData()));
        }
    }

    private void handleError(Throwable throwable) {
        Log.e(TAG, "ERROR: " + throwable.getMessage());
    }

    public void setEmployees(List<EmployeeModel> employeeModels) {
        employees.setValue(employeeModels);
    }
}
