package com.pockeyt.cloverpay.models;

public class CustomerPusherModel {
    private String type;
    private CustomerModel customer;

    public CustomerPusherModel(String type, CustomerModel customer) {
        this.type = type;
        this.customer = customer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CustomerModel getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerModel customer) {
        this.customer = customer;
    }
}
