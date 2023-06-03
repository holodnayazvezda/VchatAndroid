package com.example.vchatmessenger.core.api.server;

import static com.example.vchatmessenger.core.constants.Constants.builder;
import static com.example.vchatmessenger.core.constants.Constants.serverUrl;

import com.example.vchatmessenger.api.GroupControllerApi;
import com.example.vchatmessenger.dto.CreateGroupDto;
import com.example.vchatmessenger.dto.Group;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GroupRestClient {
    public static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static GroupControllerApi getApiWithAuth(String nickname, String password) {
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
        return retrofit.create(GroupControllerApi.class);
    }

    public static Group getResultOfGroupCalls(Call<Group> call) {
        try {
            Response<Group> r = call.execute();
            return r.body();
        } catch (IOException e) {
            return null;
        }
    }

    public static List<Group> getResultOfGroupsCalls(Call<List<Group>> call) {
        try {
            Response<List<Group>> r = call.execute();
            return r.body();
        } catch (IOException e) {
            return null;
        }
    }

    public static Group createGroup(String userNickname, String userPassword, CreateGroupDto createGroupDto) {
        return getResultOfGroupCalls(getApiWithAuth(userNickname, userPassword).create2(createGroupDto));
    }

    public static List<Group> searchChatsWithOffset(String userNickname, String userPassword, String chatName, int limit, int offset) {
        return getResultOfGroupsCalls(
                getApiWithAuth(userNickname, userPassword).searchChatsWithOffset(chatName, limit, offset)
        );
    }

    public static Group getChat(String userNickname, String userPassword, Long chatId) {
        return getResultOfGroupCalls(
                getApiWithAuth(userNickname, userPassword).getChat(chatId)
        );
    }

    public static Group editAll(String userNickname, String userPassword, Long groupId,  String newName, Integer newTypeOfImage, String newImageData) {
        return getResultOfGroupCalls(
                getApiWithAuth(userNickname, userPassword).editAll(groupId, newName, newTypeOfImage, newImageData)
        );
    }
}
