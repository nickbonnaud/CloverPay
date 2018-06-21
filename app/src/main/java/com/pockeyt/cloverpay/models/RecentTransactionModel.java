package com.pockeyt.cloverpay.models;

public class RecentTransactionModel {
    private Boolean hasRecentTransaction;
    private PurchasedItemModel[] purchasedItems;
    private String purchasedOnDate;
    private int tax;
    private int tip;
    private int total;

    public RecentTransactionModel(Boolean hasRecentTransaction) {
        this.hasRecentTransaction = hasRecentTransaction;
    }

    public Boolean getHasRecentTransaction() {
        return hasRecentTransaction;
    }

    public void setHasRecentTransaction(Boolean hasRecentTransaction) {
        this.hasRecentTransaction = hasRecentTransaction;
    }

    public PurchasedItemModel[] getPurchasedItems() {
        return purchasedItems;
    }

    public void setPurchasedItems(PurchasedItemModel[] purchasedItems) {
        this.purchasedItems = purchasedItems;
    }

    public String getPurchasedOnDate() {
        return purchasedOnDate;
    }

    public void setPurchasedOnDate(String purchasedOnDate) {
        this.purchasedOnDate = purchasedOnDate;
    }

    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    public int getTip() {
        return tip;
    }

    public void setTip(int tip) {
        this.tip = tip;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
