package com.pockeyt.cloverpay.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pockeyt.cloverpay.R;
import com.pockeyt.cloverpay.models.CustomerModel;
import com.pockeyt.cloverpay.ui.viewModels.SelectedCustomerViewModel;

public class CustomerViewPagerFragment extends Fragment {
    private static final String TAG = CustomerViewPagerFragment.class.getSimpleName();
    private CustomerModel mCustomer;
    private CustomerPayFragment mCustomerPayFragment;
    private CustomerDataFragment mCustomerDataFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        SelectedCustomerViewModel selectedCustomerViewModel = ViewModelProviders.of(this).get(SelectedCustomerViewModel.class);
        selectedCustomerViewModel.getCustomer().observe(this, customer -> {
            mCustomer = customer;
            String customerName = mCustomer.getFirstName() + " " + mCustomer.getLastName();
            getActivity().setTitle(customerName);
        });

        View view = inflater.inflate(R.layout.fragment_customer_viewpager, container, false);

        mCustomerPayFragment = new CustomerPayFragment();
        mCustomerDataFragment = new CustomerDataFragment();


        ViewPager viewPager = view.findViewById(R.id.customerViewPager);
        viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return position == 0 ? mCustomerPayFragment : mCustomerDataFragment;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return position == 0 ? "Charge" : "About";
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        TabLayout tabLayout = view.findViewById(R.id.customerTabLayout);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().setTitle(getResources().getString(R.string.app_name));
    }



}
