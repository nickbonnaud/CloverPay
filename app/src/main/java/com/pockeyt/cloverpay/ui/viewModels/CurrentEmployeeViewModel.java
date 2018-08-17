package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v3.employees.Employee;
import com.clover.sdk.v3.employees.EmployeeConnector;
import com.pockeyt.cloverpay.PockeytPay;
import com.pockeyt.cloverpay.models.EmployeeModel;
import com.pockeyt.cloverpay.utils.CloverEmployeeConnector;

public class CurrentEmployeeViewModel extends ViewModel {
    private MutableLiveData<EmployeeModel> currentEmployee;
    private EmployeeConnector mEmployeeConnector;


    public LiveData<EmployeeModel> getCurrentEmployee() {
        if (this.currentEmployee == null) {
            this.currentEmployee = new MutableLiveData<EmployeeModel>();
            fetchCurrentEmployee();
        }
        return this.currentEmployee;
    }


    private void fetchCurrentEmployee() {
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

    public void setCurrentEmployee(EmployeeModel currentEmployee) {
        this.currentEmployee.setValue(currentEmployee);
    }
}
