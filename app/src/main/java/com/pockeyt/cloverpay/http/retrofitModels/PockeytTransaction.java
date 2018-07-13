package com.pockeyt.cloverpay.http.retrofitModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PockeytTransaction {
    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data {

        @SerializedName("id")
        @Expose
        private int id;
        @SerializedName("business_id")
        @Expose
        private int businessId;
        @SerializedName("customer_id")
        @Expose
        private int customerId;
        @SerializedName("tax")
        @Expose
        private Long tax;
        @SerializedName("sub_total")
        @Expose
        private Long subTotal;
        @SerializedName("total")
        @Expose
        private Long total;
        @SerializedName("pos_transaction_id")
        @Expose
        private String posTransactionId;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getBusinessId() {
            return businessId;
        }

        public void setBusinessId(int businessId) {
            this.businessId = businessId;
        }

        public int getCustomerId() {
            return customerId;
        }

        public void setCustomerId(int customerId) {
            this.customerId = customerId;
        }

        public Long getTax() {
            return tax;
        }

        public void setTax(Long tax) {
            this.tax = tax;
        }

        public Long getSubTotal() {
            return subTotal;
        }

        public void setSubTotal(Long subTotal) {
            this.subTotal = subTotal;
        }

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public String getPosTransactionId() {
            return posTransactionId;
        }

        public void setPosTransactionId(String posTransactionId) {
            this.posTransactionId = posTransactionId;
        }

    }
}
