package com.pockeyt.cloverpay.ui.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.pockeyt.cloverpay.models.EmployeeModel;

import java.util.ArrayList;
import java.util.List;

public class SelectedEmployeesViewModel extends ViewModel {
    private MutableLiveData<List<EmployeeModel>> selectedEmployees;

    public LiveData<List<EmployeeModel>> getSelectedEmployees() {
        if (this.selectedEmployees == null) {
            this.selectedEmployees = new MutableLiveData<List<EmployeeModel>>();
        }
        return this.selectedEmployees;
    }


    public void toggleSelectedEmployee(EmployeeModel employee) {
        if (this.selectedEmployees ==  null || this.selectedEmployees.getValue() == null) {
            this.selectedEmployees = new MutableLiveData<List<EmployeeModel>>();
            addToSelectedEmployees(employee);
            return;
        }
        int index = this.selectedEmployees.getValue().indexOf(employee);
        if (index == -1) {
            addToSelectedEmployees(employee);
        } else {
            removeFromSelectedEmployees(index);
        }
    }

    private void removeFromSelectedEmployees(int index) {
        List<EmployeeModel> selectedEmployees = this.selectedEmployees.getValue() != null ? this.selectedEmployees.getValue() : new ArrayList<EmployeeModel>();
        selectedEmployees.remove(index);
        setSelectedEmployees(selectedEmployees);
    }

    private void addToSelectedEmployees(EmployeeModel employee) {
        List<EmployeeModel> selectedEmployees = this.selectedEmployees.getValue() != null ? this.selectedEmployees.getValue() : new ArrayList<EmployeeModel>();
        selectedEmployees.add(employee);
        setSelectedEmployees(selectedEmployees);
    }


    public void setSelectedEmployees(List<EmployeeModel> selectedEmployees) {
        if (this.selectedEmployees ==  null) {
            this.selectedEmployees = new MutableLiveData<List<EmployeeModel>>();
        }
        this.selectedEmployees.setValue(selectedEmployees);
    }
}
