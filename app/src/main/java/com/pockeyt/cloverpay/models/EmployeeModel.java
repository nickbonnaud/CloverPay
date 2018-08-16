package com.pockeyt.cloverpay.models;

public class EmployeeModel {
    private String cloverId;
    private String name;
    private String role;


    public EmployeeModel(String cloverId, String name, String role) {
        this.cloverId = cloverId;
        this.name = name;
        this.role = role;
    }

    public String getCloverId() {
        return cloverId;
    }

    public void setCloverId(String cloverId) {
        this.cloverId = cloverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
