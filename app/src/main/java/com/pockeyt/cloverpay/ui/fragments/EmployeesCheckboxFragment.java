package com.pockeyt.cloverpay.ui.fragments;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.clover.sdk.v3.employees.Employee;
import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.models.EmployeeModel;
import com.pockeyt.cloverpay.ui.viewModels.EmployeesViewModel;

import java.util.ArrayList;
import java.util.List;

public class EmployeesCheckboxFragment extends DialogFragment {
    private static final String EMPLOYEES_KEY = "employees_key";
    private static final String TAG = EmployeesCheckboxFragment.class.getSimpleName();
    List<EmployeeModel> mSelectedEmployees;
    List<EmployeeModel> mAllEmployees;
    CharSequence[] mEmployeeListOptions;
    EmployeesCheckboxListener mListener;


    public static EmployeesCheckboxFragment newInstance(List<EmployeeModel> employees) {
        EmployeesCheckboxFragment fragment = new EmployeesCheckboxFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EMPLOYEES_KEY, (ArrayList<? extends Parcelable>) employees);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mListener = (TipsTableFragment) getTargetFragment();
        mSelectedEmployees = new ArrayList<EmployeeModel>();
        mAllEmployees = getArguments().getParcelableArrayList(EMPLOYEES_KEY);
        mEmployeeListOptions = setEmployeeNames();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Employees")
                .setMultiChoiceItems(mEmployeeListOptions, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index, boolean isChecked) {
                        EmployeeModel employee = getEmployeeByName(mEmployeeListOptions[index]);
                        if (isChecked) {
                            mSelectedEmployees.add(employee);
                        } else if (mSelectedEmployees.contains(employee)) {
                            mSelectedEmployees.remove(employee);
                        }
                    }
                })
                .setPositiveButton(getString(R.string.set), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onEmployeesCheckboxPositiveClicked(EmployeesCheckboxFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onEmployeesCheckboxNegativeClicked(EmployeesCheckboxFragment.this);
                    }
                })
                .setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onEmployeesCheckboxNeutralClicked(EmployeesCheckboxFragment.this);
                    }
                });
        return builder.create();
    }


    public interface EmployeesCheckboxListener {
        public void onEmployeesCheckboxPositiveClicked(DialogFragment dialogFragment);
        public void onEmployeesCheckboxNegativeClicked(DialogFragment dialogFragment);
        public void onEmployeesCheckboxNeutralClicked(DialogFragment dialogFragment);
    }



    private EmployeeModel getEmployeeByName(CharSequence name) {
        for (EmployeeModel employee : mAllEmployees) {
            if (employee.getName().equals(name)) {
                return employee;
            }
        }
        return null;
    }

    private CharSequence[] setEmployeeNames() {
       List<String> employeeNames = new ArrayList<String>();
       for (EmployeeModel employee : mAllEmployees) {
           employeeNames.add(employee.getName());
       }
       return employeeNames.toArray(new CharSequence[employeeNames.size()]);
    }
}
