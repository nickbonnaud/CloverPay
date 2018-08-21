package com.pockeyt.cloverpay.http;

import com.pockeyt.cloverpay.http.retrofitModels.Business;
import com.pockeyt.cloverpay.http.retrofitModels.CustomerList;
import com.pockeyt.cloverpay.http.retrofitModels.EmployeeList;
import com.pockeyt.cloverpay.http.retrofitModels.PockeytTransaction;
import com.pockeyt.cloverpay.http.retrofitModels.TipsList;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface APIInterface {

    // Get Requests

    @GET("me")
    Observable<Response<Business>> doGetBusiness();

    @GET("customers")
    Observable<Response<CustomerList>> doGetCustomers();

    @GET("transaction")
    Observable<Response<PockeytTransaction>> doGetPockeytTransaction(@Query("clover") String orderId);

    @GET("tips")
    Observable<Response<TipsList>> doGetTips(@QueryMap Map<String, String> options, @Query("employeeTips[]") List<String> employeeTips);



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

    @FormUrlEncoded
    @POST("business")
    Observable<Response<Business>> doRequestSaveAuthToken(@Field("account_type") String accountType, @Field("token") String authToken, @Field("merchant_id") String merchantId,  @Field("_method") String method);

    @FormUrlEncoded
    @POST("business")
    Observable<Response<Business>> doSendTenderId(@Field("clover_tender_id") String tenderId, @Field("_method") String method);

    @FormUrlEncoded
    @POST("transaction")
    Observable<Response<JSONObject>> doRequestPostTransaction(@Field("pos_type") String posType, @Field("pos_transaction_id") String orderId, @Field("user_id") int userId, @Field("total") long total, @Field("tax") long tax, @Field("employee_id") String employeeId, @Field("transaction_id") int transactionId);

    @FormUrlEncoded
    @POST("employees")
    Observable<Response<JSONObject>> doAddRemoveEmployee(@Field("pos_employee_id") String cloverId, @Field("name") String name, @Field("role") String role, @Field("is_create") boolean isCreate);
}
