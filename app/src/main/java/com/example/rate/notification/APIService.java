package com.example.rate.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key-pYoppQEsplKURQ0PMOGhktz0T_G8m8eqG1O-h4nezo"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
