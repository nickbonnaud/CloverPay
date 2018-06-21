package com.pockeyt.cloverpay.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.adapters.CustomerGridAdapter;
import com.pockeyt.cloverpay.models.CustomerModel;
import com.pockeyt.cloverpay.ui.viewModels.CustomersViewModel;
import com.pockeyt.cloverpay.ui.viewModels.SelectedCustomerViewModel;
import com.pockeyt.cloverpay.utils.DisplayHelpers;
import com.pockeyt.cloverpay.utils.Interfaces;

public class CustomerGridPagerFragment extends Fragment implements Interfaces.OnGridCustomerSelectedInterface {
    private static final String TAG = CustomerGridPagerFragment.class.getSimpleName();
    CustomerModel[] mCustomers;
    private View mView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_customer_grid_pager, container, false);
        setViewPagerFragments();

        CustomersViewModel customersViewModel = ViewModelProviders.of(getActivity()).get(CustomersViewModel.class);
        customersViewModel.getCustomers().observe(this, customers -> {
            mCustomers = customers;
            setGridRecyclerView();
        });

        return mView;
    }

    private void setViewPagerFragments() {
        CustomerPayFragment customerPayFragment = new CustomerPayFragment();
        CustomerDataFragment customerDataFragment = new CustomerDataFragment();

        ViewPager viewPager = mView.findViewById(R.id.customerViewGridPager);
        viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return position == 0 ? customerPayFragment : customerDataFragment;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return position == 0 ? "Charge Customer" : "Customer Data";
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        TabLayout tabLayout = mView.findViewById(R.id.customerGridTabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setGridRecyclerView() {
        RecyclerView recyclerView = mView.findViewById(R.id.customerGridRecyclerView);
        CustomerGridAdapter customerGridAdapter = new CustomerGridAdapter(mCustomers, this);
        recyclerView.setAdapter(customerGridAdapter);

        float dpWidth = DisplayHelpers.getDpWidth(getActivity());
        int numColumns = (int) (dpWidth / 200);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), numColumns);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onGridCustomerSelected(CustomerModel customer) {
        SelectedCustomerViewModel selectedCustomerViewModel = ViewModelProviders.of(getActivity()).get(SelectedCustomerViewModel.class);
        selectedCustomerViewModel.setCustomer(customer);
    }
}
