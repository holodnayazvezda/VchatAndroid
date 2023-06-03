package com.example.vchatmessenger.core.api.server;

import static com.example.vchatmessenger.core.constants.Constants.builder;
import static com.example.vchatmessenger.core.constants.Constants.serverUrl;

import com.example.vchatmessenger.api.NameControllerApi;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NameRestClient {
    public static int checkName(String name) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();
        NameControllerApi api = retrofit.create(NameControllerApi.class);
        Call<Integer> call = api.checkName(name);
        try {
            Response<Integer> r = call.execute();
            if (r.body() != null) {
                return r.body();
            } else {return -1;}
        } catch (IOException e) {
            return -1;
        }
    }
}
