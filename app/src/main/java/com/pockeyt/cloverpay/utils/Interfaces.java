package com.pockeyt.cloverpay.utils;

import com.pockeyt.cloverpay.models.CustomerModel;

public class Interfaces {

    public interface OnGridCustomerSelectedInterface {
        void onGridCustomerSelected(CustomerModel customer);
    }

    public interface OnListCustomerSelectedInterface {
        void onListCustomerSelected(CustomerModel customer);
    }
}
