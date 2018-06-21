package com.pockeyt.cloverpay.models;

public class TokenModel {
    private String value;
    private int expiry;

    public TokenModel(String value, int expiry) {
        this.value = value;
        this.expiry = expiry;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getExpiry() {
        return expiry;
    }

    public void setExpiry(int expiry) {
        this.expiry = expiry;
    }
}
