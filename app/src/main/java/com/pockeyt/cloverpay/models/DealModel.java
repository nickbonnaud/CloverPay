package com.pockeyt.cloverpay.models;

public class DealModel {
    private Boolean hasDeal;
    private int dealId;
    private String dealItem;

    public DealModel(Boolean hasDeal) {
        this.hasDeal = hasDeal;
    }

    public Boolean getHasDeal() {
        return hasDeal;
    }

    public void setHasDeal(Boolean hasDeal) {
        this.hasDeal = hasDeal;
    }

    public int getDealId() {
        return dealId;
    }

    public void setDealId(int dealId) {
        this.dealId = dealId;
    }

    public String getDealItem() {
        return dealItem;
    }

    public void setDealItem(String dealItem) {
        this.dealItem = dealItem;
    }
}
