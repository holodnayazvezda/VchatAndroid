package com.example.vchatmessenger.core.api.server;

import static com.example.vchatmessenger.core.constants.Constants.builder;
import static com.example.vchatmessenger.core.constants.Constants.serverUrl;

import com.example.vchatmessenger.api.PasswordControllerApi;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PasswordRestClient {
    public static int checkPassword(String password, String password_confirmation) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        PasswordControllerApi api = retrofit.create(PasswordControllerApi.class);
        Call<Integer> call = api.checkPasswordAllConditions(password, password_confirmation);
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
