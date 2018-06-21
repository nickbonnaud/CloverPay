package com.pockeyt.cloverpay.models;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomerModel implements Parcelable {
    private int id;
    private String firstName;
    private String lastName;
    private String photoUrl;
    private String largePhotoUrl;

    private DealModel deal;
    private LoyaltyCardModel loyaltyCard;
    private RecentTransactionModel recentTransaction;
    private RecentPostInteractionModel recentPostInteraction;


    public CustomerModel(int id, String firstName, String lastName, String photoUrl, String largePhotoUrl, DealModel deal, LoyaltyCardModel loyaltyCard, RecentTransactionModel recentTransaction, RecentPostInteractionModel recentPostInteraction) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoUrl = photoUrl;
        this.largePhotoUrl = largePhotoUrl;
        this.deal = deal;
        this.loyaltyCard = loyaltyCard;
        this.recentTransaction = recentTransaction;
        this.recentPostInteraction = recentPostInteraction;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getLargePhotoUrl() {
        return largePhotoUrl;
    }

    public void setLargePhotoUrl(String largePhotoUrl) {
        this.largePhotoUrl = largePhotoUrl;
    }

    public DealModel getDeal() {
        return deal;
    }

    public void setDeal(DealModel deal) {
        this.deal = deal;
    }

    public LoyaltyCardModel getLoyaltyCard() {
        return loyaltyCard;
    }

    public void setLoyaltyCard(LoyaltyCardModel loyaltyCard) {
        this.loyaltyCard = loyaltyCard;
    }

    public RecentTransactionModel getRecentTransaction() {
        return recentTransaction;
    }

    public void setRecentTransaction(RecentTransactionModel recentTransaction) {
        this.recentTransaction = recentTransaction;
    }

    public RecentPostInteractionModel getRecentPostInteraction() {
        return recentPostInteraction;
    }

    public void setRecentPostInteraction(RecentPostInteractionModel recentPostInteraction) {
        this.recentPostInteraction = recentPostInteraction;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(photoUrl);
    }

    public static final Parcelable.Creator<CustomerModel> CREATOR = new Parcelable.Creator<CustomerModel>() {

        @Override
        public CustomerModel createFromParcel(Parcel source) {
            return new CustomerModel(source);
        }

        @Override
        public CustomerModel[] newArray(int size) {
            return new CustomerModel[size];
        }

    };

    public CustomerModel(Parcel source) {
        id = source.readInt();
        firstName = source.readString();
        lastName = source.readString();
        photoUrl = source.readString();
    }
}
