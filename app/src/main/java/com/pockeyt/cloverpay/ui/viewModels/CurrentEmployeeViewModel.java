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
import com.pockeyt.cloverpay.models.EmployeeModel;
import com.pockeyt.cloverpay.utils.CloverEmployeeConnector;

public class CurrentEmployeeViewModel extends ViewModel {
    public static final String TAG = CurrentEmployeeViewModel.class.getSimpleName();
    private MutableLiveData<EmployeeModel> currentEmployee;
    private EmployeeConnector mEmployeeConnector;


    public LiveData<EmployeeModel> getCurrentEmployee() {
        if (this.currentEmployee == null) {
            this.currentEmployee = new MutableLiveData<EmployeeModel>();
            fetchCurrentEmployee();
        }
        return this.currentEmployee;
    }


    public void fetchCurrentEmployee() {
        connectEmployeeConnector();
        mEmployeeConnector.getEmployee(new EmployeeConnector.EmployeeCallback<Employee>() {
            @Override
            public void onServiceSuccess(Employee result, ResultStatus status) {
                super.onServiceSuccess(result, status);
                EmployeeModel currentEmployee = new EmployeeModel(result.getId(), result.getName(), result.getRole().toString());
                setCurrentEmployee(currentEmployee);
            }
        });
    }

    private void connectEmployeeConnector() {
        if (mEmployeeConnector == null) {
            Context context = PockeytPay.getAppContext();
            CloverEmployeeConnector cloverEmployeeConnector = new CloverEmployeeConnector(context);
            mEmployeeConnector = cloverEmployeeConnector.connect();
        }
    }

    private void setCurrentEmployee(EmployeeModel currentEmployee) {
        if (this.currentEmployee == null || this.currentEmployee.getValue() == null) {
            Log.d(TAG, "1");
            if (this.currentEmployee == null) {
                Log.d(TAG, "2");
                this.currentEmployee = new MutableLiveData<EmployeeModel>();
            }
            this.currentEmployee.setValue(currentEmployee);
        } else if (!this.currentEmployee.getValue().getCloverId().equals(currentEmployee.getCloverId())) {
            Log.d(TAG, "3");
            this.currentEmployee.setValue(currentEmployee);
        }
    }
}
