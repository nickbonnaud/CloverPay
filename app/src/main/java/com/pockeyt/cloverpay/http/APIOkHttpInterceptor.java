package com.pockeyt.cloverpay.http;

import com.pockeyt.cloverpay.models.TokenModel;
import com.pockeyt.cloverpay.utils.TokenHandler;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class APIOkHttpInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        if ((originalRequest.url()).toString().contains("/login")) {
            return chain.proceed(originalRequest);
        } else {
            TokenHandler tokenHandler = new TokenHandler();
            TokenModel token = tokenHandler.getToken();

            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token.getValue())
                    .header("Accept", "application/json")
                    .build();
            return chain.proceed(newRequest);
        }
    }
}
