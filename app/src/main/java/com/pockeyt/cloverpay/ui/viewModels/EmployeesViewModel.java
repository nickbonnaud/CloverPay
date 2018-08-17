package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.util.Log;

import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v3.employees.Employee;
import com.clover.sdk.v3.employees.EmployeeConnector;
import com.pockeyt.cloverpay.PockeytPay;
import com.pockeyt.cloverpay.handlers.EmployeeHandler;
import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.http.retrofitModels.EmployeeList;
import com.pockeyt.cloverpay.models.EmployeeModel;
import com.pockeyt.cloverpay.utils.CloverEmployeeConnector;

import java.io.IOException;
import java.util.ArrayList;
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
    private EmployeeConnector mEmployeeConnector;

    public LiveData<List<EmployeeModel>> getEmployees() {
        if (this.employees == null) {
            this.employees = new MutableLiveData<List<EmployeeModel>>();
            fetchEmployees();
        }
        return this.employees;
    }

    private void fetchEmployees() {
        connectEmployeeConnector();
        mEmployeeConnector.getEmployees(new EmployeeConnector.EmployeeCallback<List<Employee>>() {
            @Override
            public void onServiceSuccess(List<Employee> result, ResultStatus status) {
                super.onServiceSuccess(result, status);
                List<EmployeeModel> employeeModels = formatEmployeesData(result);
                setEmployees(employeeModels);
            }
        });
    }

    private List<EmployeeModel> formatEmployeesData(List<Employee> employees) {
        List<EmployeeModel> employeeModels = new ArrayList<EmployeeModel>();
        for (Employee employee : employees) {
            employeeModels.add(new EmployeeModel(employee.getId(), employee.getName(), employee.getRole().toString()));
        }
        return employeeModels;
    }


    private void setEmployees(List<EmployeeModel> employeeModels) {
        employees.setValue(employeeModels);
    }



    private void connectEmployeeConnector() {
        if (mEmployeeConnector == null) {
            Context context = PockeytPay.getAppContext();
            CloverEmployeeConnector cloverEmployeeConnector = new CloverEmployeeConnector(context);
            mEmployeeConnector = cloverEmployeeConnector.connect();
        }
    }
}
