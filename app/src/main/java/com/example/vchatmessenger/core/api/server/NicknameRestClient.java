package com.example.vchatmessenger.core.api.server;

import static com.example.vchatmessenger.core.constants.Constants.builder;
import static com.example.vchatmessenger.core.constants.Constants.serverUrl;

import com.example.vchatmessenger.api.NicknameControllerApi;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NicknameRestClient {
    public static int checkNicknameForUser(String nickname) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        NicknameControllerApi api = retrofit.create(NicknameControllerApi.class);
        Call<Integer> call = api.checkNicknameForUser(nickname);
        try {
            Response<Integer> r = call.execute();
            if (r.body() != null) {
                return r.body();
            } else {return -1;}
        } catch (IOException e) {
            return -1;
        }
    }

    public static int checkNicknameForChannel(String nickname) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        NicknameControllerApi api = retrofit.create(NicknameControllerApi.class);
        Call<Integer> call = api.checkNicknameForChannel(nickname);
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
