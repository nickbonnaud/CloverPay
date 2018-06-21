package com.pockeyt.cloverpay.models;

public class LoyaltyCardModel {
    private Boolean hasReward;
    private int loyaltyCardId;
    private int unRedeemedCount;
    private int totalRewardsEarned;
    private String loyaltyReward;

    public LoyaltyCardModel(Boolean hasReward) {
        this.hasReward = hasReward;

    }

    public Boolean getHasReward() {
        return hasReward;
    }

    public void setHasReward(Boolean hasReward) {
        this.hasReward = hasReward;
    }

    public int getLoyaltyCardId() {
        return loyaltyCardId;
    }

    public void setLoyaltyCardId(int loyaltyCardId) {
        this.loyaltyCardId = loyaltyCardId;
    }

    public int getUnRedeemedCount() {
        return unRedeemedCount;
    }

    public void setUnRedeemedCount(int unRedeemedCount) {
        this.unRedeemedCount = unRedeemedCount;
    }

    public int getTotalRewardsEarned() {
        return totalRewardsEarned;
    }

    public void setTotalRewardsEarned(int totalRewardsEarned) {
        this.totalRewardsEarned = totalRewardsEarned;
    }

    public String getLoyaltyReward() {
        return loyaltyReward;
    }

    public void setLoyaltyReward(String loyaltyReward) {
        this.loyaltyReward = loyaltyReward;
    }
}
