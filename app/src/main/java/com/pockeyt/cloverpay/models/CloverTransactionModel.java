package com.pockeyt.cloverpay.models;

public class CloverTransactionModel {
    private long amount;
    private long taxAmount;
    private String orderId;
    private String merchantId;
    private String employeeId;

    public CloverTransactionModel(long amount, long taxAmount, String orderId, String merchantId, String employeeId) {
        this.amount = amount;
        this.taxAmount = taxAmount;
        this.orderId = orderId;
        this.merchantId = merchantId;
        this.employeeId = employeeId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(long taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}
