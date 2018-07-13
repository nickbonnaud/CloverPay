package com.pockeyt.cloverpay.models;

public class PockeytTransactionModel {
    private int id;
    private int businessId;
    private int customerId;
    private long tax;
    private long subTotal;
    private long total;
    private String orderId;

    public PockeytTransactionModel(int id, int businessId, int customerId, long tax, long subTotal, long total, String orderId) {
        this.id = id;
        this.businessId = businessId;
        this.customerId = customerId;
        this.tax = tax;
        this.subTotal = subTotal;
        this.total = total;
        this.orderId = orderId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public long getTax() {
        return tax;
    }

    public void setTax(long tax) {
        this.tax = tax;
    }

    public long getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(long subTotal) {
        this.subTotal = subTotal;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
