package com.pockeyt.cloverpay.handlers;

import com.pockeyt.cloverpay.http.retrofitModels.CustomerList;
import com.pockeyt.cloverpay.models.CustomerModel;
import com.pockeyt.cloverpay.models.DealModel;
import com.pockeyt.cloverpay.models.LoyaltyCardModel;
import com.pockeyt.cloverpay.models.PurchasedItemModel;
import com.pockeyt.cloverpay.models.RecentPostInteractionModel;
import com.pockeyt.cloverpay.models.RecentTransactionModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CustomerHandler {

    public static CustomerModel[] setCustomers(List<CustomerList.Datum> data) {
        CustomerModel[] customers = new CustomerModel[data.size()];

        for (int i = 0; i < data.size(); i++) {
            CustomerModel customer = setCustomersData(data.get(i));
            customers[i] = customer;
        }
        return customers;
    }

    public static CustomerModel setCustomersData(CustomerList.Datum datum) {
        DealModel deal = setCustomersDeal(datum.getDealData());
        LoyaltyCardModel loyaltyCard = setCustomersLoyaltyCard(datum.getLoyaltyCard());
        RecentTransactionModel recentTransaction = setCustomersRecentTransaction(datum.getRecentTransaction());
        RecentPostInteractionModel recentPostInteraction = setCustomersRecentPostInteractions(datum.getLastPostInteractions());
        return new CustomerModel(
                datum.getId(),
                datum.getFirstName(),
                datum.getLastName(),
                datum.getPhotoPath(),
                datum.getLargePhotoPath(),
                deal,
                loyaltyCard,
                recentTransaction,
                recentPostInteraction
        );
    }

    private static DealModel setCustomersDeal(CustomerList.DealData dealData) {
        DealModel deal = new DealModel(dealData.getHasDeal());
        if (deal.getHasDeal()) {
            deal.setDealId(dealData.getDealId());
            deal.setDealItem(dealData.getDealItem());
        }
        return deal;
    }

    private static LoyaltyCardModel setCustomersLoyaltyCard(CustomerList.LoyaltyCard loyaltyCardData) {
        LoyaltyCardModel loyaltyCard = new LoyaltyCardModel(loyaltyCardData.getHasReward());
        if (loyaltyCard.getHasReward()) {
            loyaltyCard.setLoyaltyCardId(loyaltyCardData.getLoyaltyCardId());
            loyaltyCard.setUnRedeemedCount(loyaltyCardData.getUnredeemedCount());
            loyaltyCard.setTotalRewardsEarned(loyaltyCardData.getTotalRewardsEarned());
            loyaltyCard.setLoyaltyReward(loyaltyCardData.getLoyaltyReward());
        }
        return loyaltyCard;
    }

    private static RecentTransactionModel setCustomersRecentTransaction(CustomerList.RecentTransaction recentTransactionData) {
        RecentTransactionModel recentTransaction = new RecentTransactionModel(recentTransactionData.getHasRecent());
        if (recentTransaction.getHasRecentTransaction()) {
            recentTransaction.setPurchasedItems(setCustomersPurchasedItems(recentTransactionData.getPurchasedItems()));
            recentTransaction.setPurchasedOnDate(recentTransactionData.getPurchasedOn().getDate());
            recentTransaction.setTax(recentTransactionData.getTax());
            recentTransaction.setTip(recentTransactionData.getTip());
            recentTransaction.setTotal(recentTransactionData.getTotal());
        }
        return recentTransaction;
    }

    private static PurchasedItemModel[] setCustomersPurchasedItems(List<CustomerList.PurchasedItem> purchasedItemsData) {
        PurchasedItemModel[] purchasedItems = new PurchasedItemModel[purchasedItemsData.size()];
        for (int i = 0; i < purchasedItemsData.size(); i++) {
            PurchasedItemModel purchasedItem = new PurchasedItemModel(
                    purchasedItemsData.get(i).getId(),
                    purchasedItemsData.get(i).getName(),
                    purchasedItemsData.get(i).getPrice(),
                    purchasedItemsData.get(i).getQuantity()
            );
            purchasedItems[i] = purchasedItem;
        }
        return purchasedItems;
    }

    private static RecentPostInteractionModel setCustomersRecentPostInteractions(CustomerList.LastPostInteractions lastPostInteractionsData) {
        RecentPostInteractionModel recentPostInteraction = new RecentPostInteractionModel(lastPostInteractionsData.getHasRecent());
        if (recentPostInteraction.getHasRecentPostInteraction()) {
            recentPostInteraction.setViewedOn(lastPostInteractionsData.getViewedOn().getDate());
            recentPostInteraction.setRedeemable(lastPostInteractionsData.getIsRedeemable() == 1);
            recentPostInteraction.setEvent(lastPostInteractionsData.getIsEvent());
            recentPostInteraction.setMessage(lastPostInteractionsData.getMessage());
            recentPostInteraction.setTitle(lastPostInteractionsData.getTitle());
            recentPostInteraction.setBody(lastPostInteractionsData.getBody());
            recentPostInteraction.setPostImageUrl(lastPostInteractionsData.getPostImageUrl());
        }
        return recentPostInteraction;
    }








    public static CustomerModel setCustomer(JSONObject data) throws JSONException {
        DealModel deal = setCustomerDeal(data.getJSONObject("deal_data"));
        LoyaltyCardModel loyaltyCard = setCustomerLoyaltyCard(data.getJSONObject("loyalty_card"));
        RecentTransactionModel recentTransaction = setCustomerRecentTransaction(data.getJSONObject("recent_transaction"));
        RecentPostInteractionModel recentPostInteraction = setCustomerRecentPostInteractions(data.getJSONObject("last_post_interactions"));
        return new CustomerModel(
                data.getInt("id"),
                data.getString("first_name"),
                data.getString("last_name"),
                data.getString("photo_path"),
                data.getString("large_photo_path"),
                deal,
                loyaltyCard,
                recentTransaction,
                recentPostInteraction
        );
    }

    private static DealModel setCustomerDeal(JSONObject dealData) throws JSONException {
        DealModel deal = new DealModel(dealData.getBoolean("has_deal"));
        if (deal.getHasDeal()) {
            deal.setDealId(dealData.getInt("deal_id"));
            deal.setDealItem(dealData.getString("deal_item"));
        }
        return deal;
    }

    private static LoyaltyCardModel setCustomerLoyaltyCard(JSONObject loyaltyCardData) throws JSONException {
        LoyaltyCardModel loyaltyCard = new LoyaltyCardModel(loyaltyCardData.getBoolean("has_reward"));
        if (loyaltyCard.getHasReward()) {
            loyaltyCard.setLoyaltyCardId(loyaltyCardData.getInt("loyalty_card_id"));
            loyaltyCard.setUnRedeemedCount(loyaltyCardData.getInt("unredeemed_count"));
            loyaltyCard.setTotalRewardsEarned(loyaltyCardData.getInt("total_rewards_earned"));
            loyaltyCard.setLoyaltyReward(loyaltyCardData.getString("loyalty_reward"));
        }
        return loyaltyCard;
    }

    private static RecentTransactionModel setCustomerRecentTransaction(JSONObject recentTransactionData) throws JSONException {
        RecentTransactionModel recentTransaction = new RecentTransactionModel(recentTransactionData.getBoolean("has_recent"));
        if (recentTransaction.getHasRecentTransaction()) {
            recentTransaction.setPurchasedItems(setCustomerPurchasedItems(recentTransactionData.getJSONArray("purchased_items")));
            recentTransaction.setPurchasedOnDate(recentTransactionData.getJSONObject("purchased_on").getString("date"));
            recentTransaction.setTax(recentTransactionData.getInt("tax"));
            recentTransaction.setTip(recentTransactionData.getInt("tip"));
            recentTransaction.setTotal(recentTransactionData.getInt("total"));
        }
        return recentTransaction;
    }

    private static PurchasedItemModel[] setCustomerPurchasedItems(JSONArray purchasedItemsData) throws JSONException {
        PurchasedItemModel[] purchasedItems = new PurchasedItemModel[purchasedItemsData.length()];
        for (int i = 0; i < purchasedItemsData.length(); i++) {
            PurchasedItemModel purchasedItem = new PurchasedItemModel(
                    purchasedItemsData.getJSONObject(i).getInt("id"),
                    purchasedItemsData.getJSONObject(i).getString("name"),
                    purchasedItemsData.getJSONObject(i).getInt("price"),
                    purchasedItemsData.getJSONObject(i).getInt("quantity")
            );
            purchasedItems[i] = purchasedItem;
        }
        return purchasedItems;
    }


    private static RecentPostInteractionModel setCustomerRecentPostInteractions(JSONObject lastPostInteractionsData) throws JSONException {
        RecentPostInteractionModel recentPostInteraction = new RecentPostInteractionModel(lastPostInteractionsData.getBoolean("has_recent"));
        if (recentPostInteraction.getHasRecentPostInteraction()) {
            recentPostInteraction.setViewedOn(lastPostInteractionsData.getJSONObject("viewed_on").getString("date"));
            recentPostInteraction.setRedeemable(lastPostInteractionsData.getInt("is_redeemable") == 1);
            recentPostInteraction.setEvent(lastPostInteractionsData.getBoolean("is_event"));
            recentPostInteraction.setMessage(lastPostInteractionsData.getString("message"));
            recentPostInteraction.setTitle(lastPostInteractionsData.getString("title"));
            recentPostInteraction.setBody(lastPostInteractionsData.getString("body"));
            recentPostInteraction.setPostImageUrl(lastPostInteractionsData.getString("postImageUrl"));
        }
        return recentPostInteraction;
    }
}
