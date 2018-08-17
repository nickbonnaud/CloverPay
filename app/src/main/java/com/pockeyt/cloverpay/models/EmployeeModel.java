package com.pockeyt.cloverpay.models;

import android.os.Parcel;
import android.os.Parcelable;

public class EmployeeModel implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cloverId);
        dest.writeString(name);
        dest.writeString(role);
    }

    public static final Parcelable.Creator<EmployeeModel> CREATOR = new Parcelable.Creator<EmployeeModel>() {

        @Override
        public EmployeeModel createFromParcel(Parcel source) {
            return new EmployeeModel(source);
        }

        @Override
        public EmployeeModel[] newArray(int size) {
            return new EmployeeModel[size];
        }
    };

    public EmployeeModel(Parcel source) {
        cloverId = source.readString();
        name = source.readString();
        role = source.readString();
    }
}
