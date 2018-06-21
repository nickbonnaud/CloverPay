package com.pockeyt.cloverpay.models;

public class RecentPostInteractionModel {
    private Boolean hasRecentPostInteraction;
    private String viewedOn;
    private Boolean isRedeemable;
    private Boolean isEvent;
    private String message;
    private String title;
    private String body;
    private String postImageUrl;

    public RecentPostInteractionModel(Boolean hasRecentPostInteraction) {
        this.hasRecentPostInteraction = hasRecentPostInteraction;
    }

    public Boolean getHasRecentPostInteraction() {
        return hasRecentPostInteraction;
    }

    public void setHasRecentPostInteraction(Boolean hasRecentPostInteraction) {
        this.hasRecentPostInteraction = hasRecentPostInteraction;
    }

    public String getViewedOn() {
        return viewedOn;
    }

    public void setViewedOn(String viewedOn) {
        this.viewedOn = viewedOn;
    }

    public Boolean getRedeemable() {
        return isRedeemable;
    }

    public void setRedeemable(Boolean redeemable) {
        isRedeemable = redeemable;
    }

    public Boolean getEvent() {
        return isEvent;
    }

    public void setEvent(Boolean event) {
        isEvent = event;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }
}
