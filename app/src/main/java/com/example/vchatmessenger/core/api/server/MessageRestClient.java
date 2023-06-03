package com.example.vchatmessenger.core.api.server;

import static com.example.vchatmessenger.core.constants.Constants.builder;
import static com.example.vchatmessenger.core.constants.Constants.serverUrl;

import com.example.vchatmessenger.api.MessageControllerApi;
import com.example.vchatmessenger.dto.CreateMessageDto;
import com.example.vchatmessenger.dto.Message;

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

public class MessageRestClient {
    public static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static MessageControllerApi getApiWithAuth(String nickname, String password) {
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
        return retrofit.create(MessageControllerApi.class);
    }

    public static Message getResultOfMessageCalls(Call<Message> call) {
        try {
            Response<Message> r = call.execute();
            return r.body();
        } catch (IOException e) {
            return null;
        }
    }

    public static List<Message> getResultOfMessagesCalls(Call<List<Message>> call) {
        try {
            Response<List<Message>> r = call.execute();
            return r.body();
        } catch (IOException e) {
            return null;
        }
    }

    public static List<Integer> getResultOfIntegerCalls(Call<List<Integer>> call) {
        try {
            Response<List<Integer>> r = call.execute();
            return r.body();
        } catch (IOException e) {
            return null;
        }
    }

    public static int getResultOfVoidCalls(Call<Void> call) {
        try {
            Response<Void> r = call.execute();
            if (r.code() == 200) {
                return 1;
            } else {
                return 0;
            }
        } catch (IOException e) {
            return -1;
        }
    }

    public static Message createMessage(String userNickname, String userPassword, CreateMessageDto dto) {
        return getResultOfMessageCalls(
                getApiWithAuth(userNickname, userPassword).create1(dto)
        );
    }

    public static Message addReader(String userNickname, String userPassword, Long messageId) {
        return getResultOfMessageCalls(
                getApiWithAuth(userNickname, userPassword).addReader(messageId)
        );
    }

    public static List<Integer> getPositionsOfFoundMessages(String userNickname, String userPassword, Long groupId, String content) {
        return getResultOfIntegerCalls(
                getApiWithAuth(userNickname, userPassword).getPositionsOfFoundMessages(groupId, content)
        );
    }

    public static Message getLastMessage(String userNickname, String userPassword, Long chatId) {
        return getResultOfMessageCalls(
                getApiWithAuth(userNickname, userPassword).getLastMessage(chatId)
        );
    }

    public static List<Message> getMessagesWithOffset(String userNickname, String userPassword, Long chatId, int limit, int offset) {
        return getResultOfMessagesCalls(
                getApiWithAuth(userNickname, userPassword).getMessagesWithOffset(chatId, limit, offset)
        );
    }

    public static int deleteMessage(String userNickname, String userPassword, Long messageId) {
        return getResultOfVoidCalls(
                getApiWithAuth(userNickname, userPassword).delete1(messageId)
        );
    }
}
