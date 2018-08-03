package com.pockeyt.cloverpay.http.retrofitModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CustomerList {

    @SerializedName("data")
    @Expose
    private List<Datum> data = null;

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public class Datum {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("first_name")
        @Expose
        private String firstName;
        @SerializedName("last_name")
        @Expose
        private String lastName;
        @SerializedName("photo_path")
        @Expose
        private String photoPath;
        @SerializedName("large_photo_path")
        @Expose
        private String largePhotoPath;
        @SerializedName("recent_transaction")
        @Expose
        private RecentTransaction recentTransaction;
        @SerializedName("last_post_interactions")
        @Expose
        private LastPostInteractions lastPostInteractions;
        @SerializedName("deal_data")
        @Expose
        private DealData dealData;
        @SerializedName("loyalty_card")
        @Expose
        private LoyaltyCard loyaltyCard;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
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

        public String getPhotoPath() {
            return photoPath;
        }

        public void setPhotoPath(String photoPath) {
            this.photoPath = photoPath;
        }

        public String getLargePhotoPath() {
            return largePhotoPath;
        }

        public void setLargePhotoPath(String largePhotoPath) {
            this.largePhotoPath = largePhotoPath;
        }

        public RecentTransaction getRecentTransaction() {
            return recentTransaction;
        }

        public void setRecentTransaction(RecentTransaction recentTransaction) {
            this.recentTransaction = recentTransaction;
        }

        public LastPostInteractions getLastPostInteractions() {
            return lastPostInteractions;
        }

        public void setLastPostInteractions(LastPostInteractions lastPostInteractions) {
            this.lastPostInteractions = lastPostInteractions;
        }

        public DealData getDealData() {
            return dealData;
        }

        public void setDealData(DealData dealData) {
            this.dealData = dealData;
        }

        public LoyaltyCard getLoyaltyCard() {
            return loyaltyCard;
        }

        public void setLoyaltyCard(LoyaltyCard loyaltyCard) {
            this.loyaltyCard = loyaltyCard;
        }

    }

    public class DealData {

        @SerializedName("has_deal")
        @Expose
        private Boolean hasDeal;
        @SerializedName("deal_id")
        @Expose
        private Integer dealId;
        @SerializedName("deal_item")
        @Expose
        private String dealItem;

        public Boolean getHasDeal() {
            return hasDeal;
        }

        public void setHasDeal(Boolean hasDeal) {
            this.hasDeal = hasDeal;
        }

        public Integer getDealId() {
            return dealId;
        }

        public void setDealId(Integer dealId) {
            this.dealId = dealId;
        }

        public String getDealItem() {
            return dealItem;
        }

        public void setDealItem(String dealItem) {
            this.dealItem = dealItem;
        }

    }

    public class LastPostInteractions {

        @SerializedName("has_recent")
        @Expose
        private Boolean hasRecent;
        @SerializedName("viewed_on")
        @Expose
        private ViewedOn viewedOn;
        @SerializedName("is_redeemable")
        @Expose
        private Integer isRedeemable;
        @SerializedName("is_event")
        @Expose
        private Boolean isEvent;
        @SerializedName("message")
        @Expose
        private String message;
        @SerializedName("body")
        @Expose
        private String body;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("postImageUrl")
        @Expose
        private String postImageUrl;

        public Boolean getHasRecent() {
            return hasRecent;
        }

        public void setHasRecent(Boolean hasRecent) {
            this.hasRecent = hasRecent;
        }

        public ViewedOn getViewedOn() {
            return viewedOn;
        }

        public void setViewedOn(ViewedOn viewedOn) {
            this.viewedOn = viewedOn;
        }

        public Integer getIsRedeemable() {
            return isRedeemable;
        }

        public void setIsRedeemable(Integer isRedeemable) {
            this.isRedeemable = isRedeemable;
        }

        public Boolean getIsEvent() {
            return isEvent;
        }

        public void setIsEvent(Boolean isEvent) {
            this.isEvent = isEvent;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPostImageUrl() {
            return postImageUrl;
        }

        public void setPostImageUrl(String postImageUrl) {
            this.postImageUrl = postImageUrl;
        }

    }

    public class LoyaltyCard {

        @SerializedName("has_reward")
        @Expose
        private Boolean hasReward;
        @SerializedName("loyalty_card_id")
        @Expose
        private Integer loyaltyCardId;
        @SerializedName("unredeemed_count")
        @Expose
        private Integer unredeemedCount;
        @SerializedName("total_rewards_earned")
        @Expose
        private Integer totalRewardsEarned;
        @SerializedName("loyalty_reward")
        @Expose
        private String loyaltyReward;

        public Boolean getHasReward() {
            return hasReward;
        }

        public void setHasReward(Boolean hasReward) {
            this.hasReward = hasReward;
        }

        public Integer getLoyaltyCardId() {
            return loyaltyCardId;
        }

        public void setLoyaltyCardId(Integer loyaltyCardId) {
            this.loyaltyCardId = loyaltyCardId;
        }

        public Integer getUnredeemedCount() {
            return unredeemedCount;
        }

        public void setUnredeemedCount(Integer unredeemedCount) {
            this.unredeemedCount = unredeemedCount;
        }

        public Integer getTotalRewardsEarned() {
            return totalRewardsEarned;
        }

        public void setTotalRewardsEarned(Integer totalRewardsEarned) {
            this.totalRewardsEarned = totalRewardsEarned;
        }

        public String getLoyaltyReward() {
            return loyaltyReward;
        }

        public void setLoyaltyReward(String loyaltyReward) {
            this.loyaltyReward = loyaltyReward;
        }

    }

    public class PurchasedItem {

        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("price")
        @Expose
        private Integer price;
        @SerializedName("quantity")
        @Expose
        private Integer quantity;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
            this.price = price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

    }

    public class PurchasedOn {

        @SerializedName("date")
        @Expose
        private String date;
        @SerializedName("timezone_type")
        @Expose
        private Integer timezoneType;
        @SerializedName("timezone")
        @Expose
        private String timezone;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Integer getTimezoneType() {
            return timezoneType;
        }

        public void setTimezoneType(Integer timezoneType) {
            this.timezoneType = timezoneType;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

    }

    public class RecentTransaction {

        @SerializedName("has_recent")
        @Expose
        private Boolean hasRecent;
        @SerializedName("purchased_items")
        @Expose
        private List<PurchasedItem> purchasedItems = null;
        @SerializedName("purchased_on")
        @Expose
        private PurchasedOn purchasedOn;
        @SerializedName("tax")
        @Expose
        private Integer tax;
        @SerializedName("tip")
        @Expose
        private Integer tip;
        @SerializedName("total")
        @Expose
        private Integer total;

        public Boolean getHasRecent() {
            return hasRecent;
        }

        public void setHasRecent(Boolean hasRecent) {
            this.hasRecent = hasRecent;
        }

        public List<PurchasedItem> getPurchasedItems() {
            return purchasedItems;
        }

        public void setPurchasedItems(List<PurchasedItem> purchasedItems) {
            this.purchasedItems = purchasedItems;
        }

        public PurchasedOn getPurchasedOn() {
            return purchasedOn;
        }

        public void setPurchasedOn(PurchasedOn purchasedOn) {
            this.purchasedOn = purchasedOn;
        }

        public Integer getTax() {
            return tax;
        }

        public void setTax(Integer tax) {
            this.tax = tax;
        }

        public Integer getTip() {
            return tip;
        }

        public void setTip(Integer tip) {
            this.tip = tip;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

    }

    public class ViewedOn {

        @SerializedName("date")
        @Expose
        private String date;
        @SerializedName("timezone_type")
        @Expose
        private Integer timezoneType;
        @SerializedName("timezone")
        @Expose
        private String timezone;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Integer getTimezoneType() {
            return timezoneType;
        }

        public void setTimezoneType(Integer timezoneType) {
            this.timezoneType = timezoneType;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

    }
}