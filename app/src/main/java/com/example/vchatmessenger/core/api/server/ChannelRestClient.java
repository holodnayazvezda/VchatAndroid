package com.example.vchatmessenger.core.api.server;

import static com.example.vchatmessenger.core.constants.Constants.builder;
import static com.example.vchatmessenger.core.constants.Constants.serverUrl;

import com.example.vchatmessenger.api.ChannelControllerApi;
import com.example.vchatmessenger.dto.Channel;
import com.example.vchatmessenger.dto.CreateChannelDto;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChannelRestClient {
    public static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static ChannelControllerApi getApiWithAuth(String nickname, String password) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .readTimeout(6, TimeUnit.SECONDS)
                .authenticator((route, response) -> {
                    Request request = response.request();
                    if (request.header("Authorization") != null)
                        // Логин и пароль неверны
                        return null;
                    return request.newBuilder()
                            .header("Authorization", Credentials.basic(nickname.toLowerCase().strip(), password))
                            .build();
                });
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ChannelControllerApi.class);
    }

    public static Channel getResultOfChannelCalls(Call<Channel> call) {
        try {
            Response<Channel> r = call.execute();
            return r.body();
        } catch (IOException e) {
            return null;
        }
    }

    public static Channel createChannel(String userNickname, String userPassword, CreateChannelDto createChannelDto) {
        return getResultOfChannelCalls(getApiWithAuth(userNickname, userPassword).create3(createChannelDto));
    }

    public static Channel getById(String userNickname, String userPassword, Long channelId) {
        return getResultOfChannelCalls(getApiWithAuth(userNickname, userPassword).getById(channelId));
    }

    public static Channel editAll(String userNickname, String userPassword, Long channelId, String newName, String newNickname, Integer newTypeOfImage, String newImageData) {
        return getResultOfChannelCalls(
                getApiWithAuth(userNickname, userPassword).editAll1(
                        channelId, newName, newNickname, newTypeOfImage, newImageData
                )
        );
    }
}
