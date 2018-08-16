package com.pockeyt.cloverpay.handlers;

import android.util.Log;

import com.clover.sdk.v3.employees.Employee;
import com.pockeyt.cloverpay.http.APIClient;
import com.pockeyt.cloverpay.http.APIInterface;
import com.pockeyt.cloverpay.http.retrofitModels.EmployeeList;
import com.pockeyt.cloverpay.models.EmployeeModel;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class EmployeeHandler {
    private static final String TAG = EmployeeHandler.class.getSimpleName();
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public List<EmployeeModel> setEmployees(List<EmployeeList.Datum> data) {
        List<EmployeeModel> employees = new ArrayList<EmployeeModel>();

        for (int i = 0; i < data.size(); i++) {
            EmployeeModel employee = setEmployeeData(data.get(i));
            employees.add(employee);
        }
        return employees;
    }

    private EmployeeModel setEmployeeData(EmployeeList.Datum datum) {
        return new EmployeeModel(
                datum.getEmployeeId(),
                datum.getName(),
                datum.getRole()
        );
    }

    public List<EmployeeModel> syncEmployees(List<EmployeeModel> pockeytEmployees, List<Employee> cloverEmployees) {
        for (Employee cloverEmployee : cloverEmployees) {
            if (!pockeytEmployeesContainCloverEmployee(pockeytEmployees, cloverEmployee)) {
                EmployeeModel pockeytEmployee = new EmployeeModel(cloverEmployee.getId(), cloverEmployee.getName(), cloverEmployee.getRole().toString());
                addRemoveEmployeeFromPockeyt(pockeytEmployee, true);
                pockeytEmployees.add(pockeytEmployee);
            }
        }
        
        for (EmployeeModel pockeytEmployee : pockeytEmployees) {
            if (!cloverEmployeesContainPockeytEmployee(cloverEmployees, pockeytEmployee)) {
                addRemoveEmployeeFromPockeyt(pockeytEmployee, false);
                pockeytEmployees.remove(pockeytEmployee);
            }
        }
        return pockeytEmployees;
    }

    private void addRemoveEmployeeFromPockeyt(EmployeeModel pockeytEmployee, boolean isCreate) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Observable<Response<JSONObject>> employeeObservable = apiInterface.doAddRemoveEmployee(pockeytEmployee.getCloverId(), pockeytEmployee.getName(), pockeytEmployee.getRole(), isCreate);

        Disposable disposable = employeeObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResult, this::handleError);
        mCompositeDisposable.add(disposable);
    }

    private void handleError(Throwable throwable) {
        Log.e(TAG, "Error: " + throwable.getMessage());
        mCompositeDisposable.dispose();
    }

    private void handleResult(Response<JSONObject> response) throws IOException {
        if (!response.isSuccessful()) {
            Log.d(TAG, "Error: " + response.errorBody().string());
        }
        mCompositeDisposable.dispose();
    }


    private static boolean pockeytEmployeesContainCloverEmployee(List<EmployeeModel> pockeytEmployees, Employee cloverEmployee) {
        for (EmployeeModel pockeytEmployee : pockeytEmployees) {
            if (pockeytEmployee.getCloverId().equals(cloverEmployee.getId())) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean cloverEmployeesContainPockeytEmployee(List<Employee> cloverEmployees, EmployeeModel pockeytEmployee) {
        for (Employee cloverEmployee : cloverEmployees) {
            if (cloverEmployee.getId().equals(pockeytEmployee.getCloverId())) {
                return true;
            }
        }
        return  false;
    }
}
