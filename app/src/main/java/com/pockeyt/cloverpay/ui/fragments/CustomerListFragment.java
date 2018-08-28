package com.pockeyt.cloverpay.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.adapters.CustomerListAdapter;
import com.pockeyt.cloverpay.models.CustomerModel;
import com.pockeyt.cloverpay.ui.activities.MainActivity;
import com.pockeyt.cloverpay.ui.viewModels.CustomersViewModel;
import com.pockeyt.cloverpay.ui.viewModels.SelectedCustomerViewModel;
import com.pockeyt.cloverpay.utils.DisplayHelpers;
import com.pockeyt.cloverpay.utils.Interfaces;

import java.util.List;

public class CustomerListFragment extends Fragment {
    private static final String TAG = CustomerListFragment.class.getSimpleName();
    private View mView;
    private List<CustomerModel> mCustomers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
       mView = inflater.inflate(R.layout.fragment_customer_list, container, false);

        CustomersViewModel customersViewModel = ViewModelProviders.of(getActivity()).get(CustomersViewModel.class);
        customersViewModel.getCustomers().observe(this, customers -> {
            mCustomers = customers;
            setRecyclerView();
        });


        SelectedCustomerViewModel selectedCustomerViewModel = ViewModelProviders.of(getActivity()).get(SelectedCustomerViewModel.class);
        CustomerModel customer = selectedCustomerViewModel.getCustomer().getValue();
        if (customer != null && !DisplayHelpers.isLandscape(getContext())) {
            CustomerViewPagerFragment fragment = new CustomerViewPagerFragment();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.placeHolder, fragment, MainActivity.CUSTOMER_VIEWPAGER_FRAGMENT);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        return mView;
    }


    private void setRecyclerView() {
        Interfaces.OnListCustomerSelectedInterface listener = (Interfaces.OnListCustomerSelectedInterface) getActivity();
        RecyclerView recyclerView = mView.findViewById(R.id.customerListRecyclerView);
        CustomerListAdapter customerListAdapter = new CustomerListAdapter(mCustomers, listener);
        recyclerView.setAdapter(customerListAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

    }
}
