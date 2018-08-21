package com.pockeyt.cloverpay.ui.fragments;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.models.EmployeeModel;
import com.pockeyt.cloverpay.ui.viewModels.SelectedEmployeesViewModel;

import java.util.ArrayList;
import java.util.List;

public class EmployeesCheckboxFragment extends DialogFragment {
    private static final String EMPLOYEES_KEY = "employees_key";
    private static final String TAG = EmployeesCheckboxFragment.class.getSimpleName();
    CharSequence[] mEmployeeListOptions;
    boolean[] mEmployeeCheckedOptions;
    SelectedEmployeesViewModel mSelectedEmployeeViewModel;


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
        EmployeesCheckboxListener listener = (TipsTableFragment) getTargetFragment();
        List<EmployeeModel> allEmployees = getArguments().getParcelableArrayList(EMPLOYEES_KEY);

        setEmployeeChoiceItemsData(allEmployees);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Employees")
                .setMultiChoiceItems(mEmployeeListOptions, mEmployeeCheckedOptions, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index, boolean isChecked) {
                        EmployeeModel employee = getEmployeeByName(mEmployeeListOptions[index], allEmployees);
                        getSelectedEmployeeViewModel().toggleSelectedEmployee(employee);
                    }
                })
                .setPositiveButton(getString(R.string.set), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onEmployeesCheckboxPositiveClicked(getSelectedEmployeeViewModel().getSelectedEmployees().getValue());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onEmployeesCheckboxNegativeClicked(EmployeesCheckboxFragment.this);
                    }
                })
                .setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getSelectedEmployeeViewModel().setSelectedEmployees(null);
                        listener.onEmployeesCheckboxNeutralClicked(EmployeesCheckboxFragment.this);
                    }
                });
        return builder.create();
    }

    private SelectedEmployeesViewModel getSelectedEmployeeViewModel() {
        return mSelectedEmployeeViewModel == null ? ViewModelProviders.of(getActivity()).get(SelectedEmployeesViewModel.class) : mSelectedEmployeeViewModel;
    }

    public interface EmployeesCheckboxListener {
        public void onEmployeesCheckboxPositiveClicked(List<EmployeeModel> selectedEmployees);
        public void onEmployeesCheckboxNegativeClicked(DialogFragment dialogFragment);
        public void onEmployeesCheckboxNeutralClicked(DialogFragment dialogFragment);
    }



    private EmployeeModel getEmployeeByName(CharSequence name, List<EmployeeModel> allEmployees) {
        for (EmployeeModel employee : allEmployees) {
            if (employee.getName().equals(name)) {
                return employee;
            }
        }
        return null;
    }

    private void setEmployeeChoiceItemsData(List<EmployeeModel> allEmployees) {
        CharSequence[] employeeNames = new CharSequence[allEmployees.size()];
        boolean[] isChecked = new boolean[allEmployees.size()];
        List<EmployeeModel> selectedEmployees = getSelectedEmployeeViewModel().getSelectedEmployees().getValue();

        for (int i = 0; i < allEmployees.size(); i++) {
            employeeNames[i] = allEmployees.get(i).getName();
            isChecked[i] = selectedEmployees != null && selectedEmployees.contains(allEmployees.get(i));
        }
       mEmployeeListOptions = employeeNames;
       mEmployeeCheckedOptions = isChecked;
    }
}
