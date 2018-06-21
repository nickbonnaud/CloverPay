package com.pockeyt.cloverpay.handlers;

import com.pockeyt.cloverpay.models.BusinessModel;

public class BusinessHandler {
    public static BusinessModel business;

    public static BusinessModel getBusiness() {
        return business;
    }

    public static void setBusiness(BusinessModel business) {
        BusinessHandler.business = business;
    }
}
