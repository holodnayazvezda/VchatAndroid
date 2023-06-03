package com.example.vchatmessenger.core.api.server;

import static com.example.vchatmessenger.core.constants.Constants.builder;
import static com.example.vchatmessenger.core.constants.Constants.serverUrl;

import com.example.vchatmessenger.api.UserControllerApi;
import com.example.vchatmessenger.dto.CreateUserDto;
import com.example.vchatmessenger.dto.Group;
import com.example.vchatmessenger.dto.User;

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

public class UserRestClient {
    public static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static UserControllerApi api = retrofit.create(UserControllerApi.class);


    public static UserControllerApi getApiWithAuth(String nickname, String password) {
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
        return retrofit.create(UserControllerApi.class);
    }

    public static int getResultOfBooleanCalls(Call<Boolean> call) {
        try {
            Response<Boolean> r = call.execute();
            if (r.body() != null) {
                if (Boolean.TRUE.equals(r.body())) {
                    return 1;
                } else {
                    return 0;
                }
            } else {return -1;}
        } catch (IOException e) {
            return -1;
        }
    }

    public static int getResultOfVoidCalls(Call<Void> call) {
        try {
            Response<Void> r = call.execute();
            if (r.code() == 200) {
                return 1;
            } else {return 0;}
        } catch (IOException e) {
            return -1;
        }
    }

    public static User getResultOfUserCalls(Call<User> call) {
        try {
            Response<User> r = call.execute();
            return r.body();
        } catch (Exception e) {
            return null;
        }
    }

    public static User createUser(CreateUserDto dto) {
        return getResultOfUserCalls(api.create(dto));
    }

    public static int exists(String userNickname) {
        return getResultOfBooleanCalls(api.exists(userNickname));
    }

    public static int checkPassword(String userNickname, String verifiablePassword) {
        return getResultOfBooleanCalls(api.checkPassword(userNickname, verifiablePassword));
    }


    public static int checkSecretKeys(String userNickname,
                                      int a, String a_value,
                                      int b, String b_value,
                                      int c, String c_value
    ) {
       return getResultOfBooleanCalls(
               api.checkSecretKeys(userNickname, a, a_value, b, b_value, c, c_value)
       );
    }

    public static int changePasswordBySecretKeys(String userNickname,
                                                  int a, String a_value,
                                                  int b, String b_value,
                                                  int c, String c_value,
                                                  String newPassword) {
        return getResultOfVoidCalls(
                api.changePasswordBySecretKeys(userNickname, a, a_value, b, b_value, c, c_value, newPassword)
        );
    }

    public static int changePassword(String nickname, String password, String newPassword) {
        return getResultOfVoidCalls(
                getApiWithAuth(nickname, password).changePassword(newPassword)
        );
    }

    public static User get(String nickname, String password) {
        return getResultOfUserCalls(getApiWithAuth(nickname, password).get());
    }

    public static User getBaseInfo(Long userId) {
        return getResultOfUserCalls(api.getBaseInfo(userId));
    }

    public static int changeName(String nickname, String password, String newName) {
        return getResultOfVoidCalls(
                getApiWithAuth(nickname, password).changeName(newName)
        );
    }

    public static int changeNickname(String nickname, String password, String newNickname) {
        return getResultOfVoidCalls(
                getApiWithAuth(nickname, password).changeNickname(newNickname)
        );
    }

    public static int changeSecretKeys(String nickname, String password, List<String> secretKeys) {
        return getResultOfVoidCalls(
                getApiWithAuth(nickname, password).changeSecretKeys(secretKeys)
        );
    }

    public static int changeImageData(String nickname, String password, String newImageData) {
        return getResultOfVoidCalls(
                getApiWithAuth(nickname, password).changeImage(newImageData)
        );
    }

    public static int setTypeOfImage(String nickname, String password, int newTypeOfImage) {
        return getResultOfVoidCalls(
                getApiWithAuth(nickname, password).setTypeOfImage(newTypeOfImage)
        );
    }

    public static int deleteUser(String nickname, String password) {
        return getResultOfVoidCalls(getApiWithAuth(nickname, password).delete());
    }

    public static List<Group> getChatsWithOffset(String nickname, String password, int limit, int offset) {
        Call<List<Group>> call = getApiWithAuth(nickname, password).getChatsWithOffset(limit, offset);
        try {
            Response<List<Group>> r = call.execute();
            return r.body();
        } catch (Exception e) {
            return null;
        }
    }

    public static int canDeleteMessage(String nickname, String password, Long messageId) {
        return getResultOfBooleanCalls(getApiWithAuth(nickname, password).canDeleteMessage(messageId));
    }

    public static int canDeleteChat(String nickname, String password, Long chatId) {
        return getResultOfBooleanCalls(
                getApiWithAuth(nickname, password).canDeleteChat(chatId)
        );
    }

    public static User removeChat(String nickname, String password, Long chatId) {
        return getResultOfUserCalls(getApiWithAuth(nickname, password).removeChat(chatId));
    }

    public static int canEditChat(String nickname, String password, Long chatId) {
        return getResultOfBooleanCalls(
                getApiWithAuth(nickname, password).canEditChat(chatId)
        );
    }

    public static User addChat(String nickname, String password, Long chatId) {
        return getResultOfUserCalls(getApiWithAuth(nickname, password).addChat(chatId));
    }
}
