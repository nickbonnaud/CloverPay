package com.pockeyt.cloverpay.models;

public class BusinessModel {

    private int id;
    private String slug;
    private String businessName;
    private TokenModel token;
    private String logoUrl;

    public BusinessModel(int id, String slug, String businessName, TokenModel token, String logoUrl) {
        this.id = id;
        this.slug = slug;
        this.businessName = businessName;
        this.token = token;
        this.logoUrl = logoUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public TokenModel getToken() {
        return token;
    }

    public void setToken(TokenModel token) {
        this.token = token;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
