package com.pockeyt.cloverpay.http;

import com.pockeyt.cloverpay.http.retrofitModels.Business;
import com.pockeyt.cloverpay.http.retrofitModels.CustomerList;

import org.json.JSONObject;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface APIInterface {

    // Get Requests

    @GET("me")
    Observable<Response<Business>> doGetBusiness();

    @GET("customers")
    Observable<Response<CustomerList>>  doGetCustomers();


    // Post Requests

    @FormUrlEncoded
    @POST("login")
    Observable<Response<Business>> doLogin(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("deal")
    Observable<Response<JSONObject>> doRequestRedeemDeal(@Field("id") int dealId, @Field("_method") String method);

    @FormUrlEncoded
    @POST("loyalty")
    Observable<Response<JSONObject>> doRequestRedeemLoyalty(@Field("id") int loyaltyCardId, @Field("_method") String method);
}
