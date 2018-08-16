package com.pockeyt.cloverpay.utils;

import android.accounts.Account;
import android.content.Context;
import android.os.IInterface;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v3.employees.Employee;
import com.clover.sdk.v3.employees.EmployeeConnector;

import java.util.List;

public class CloverEmployeeConnector implements ServiceConnector.OnServiceConnectedListener {
    private EmployeeConnector mEmployeeConnector;
    private List<Employee> mCloverEmployees;

    public CloverEmployeeConnector(Context context) {
        Context mContext = context;
        Account mAccount = CloverAccount.getAccount(context);
        this.mEmployeeConnector = new EmployeeConnector(mContext, mAccount, this);

    }


    public EmployeeConnector connect() {
        mEmployeeConnector.connect();
        return mEmployeeConnector;
    }


    @Override
    public void onServiceConnected(ServiceConnector<? extends IInterface> connector) {

    }

    @Override
    public void onServiceDisconnected(ServiceConnector<? extends IInterface> connector) {

    }
}
