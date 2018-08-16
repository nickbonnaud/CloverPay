package com.pockeyt.cloverpay.models;

public class TipsModel {
    private String date;
    private String employeeName;
    private String employeeId;
    private String billId;
    private String billTotal;
    private String tipAmountFormatted;
    private Integer tipAmount;

    public TipsModel(String date, String employeeName, String employeeId, String billId, String billTotal, String tipAmountFormatted, Integer tipAmount) {
        this.date = date;
        this.employeeName = employeeName;
        this.employeeId = employeeId;
        this.billId = billId;
        this.billTotal = billTotal;
        this.tipAmountFormatted = tipAmountFormatted;
        this.tipAmount = tipAmount;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getBillTotal() {
        return billTotal;
    }

    public void setBillTotal(String billTotal) {
        this.billTotal = billTotal;
    }

    public String getTipAmountFormatted() {
        return tipAmountFormatted;
    }

    public void setTipAmountFormatted(String tipAmountFormatted) {
        this.tipAmountFormatted = tipAmountFormatted;
    }

    public Integer getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(Integer tipAmount) {
        this.tipAmount = tipAmount;
    }
}
