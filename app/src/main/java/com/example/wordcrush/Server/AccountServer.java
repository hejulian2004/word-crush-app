package com.example.wordcrush.Server;

import androidx.annotation.NonNull;

import com.example.wordcrush.Tools.MessageCallBack;
import com.example.wordcrush.Tools.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccountServer {
    private String username;
    private String password;
    private boolean isLogin = false;
    public AccountServer(String username, String password){
        this.username = username;
        this.password = password;
    }
    public AccountServer(String username){
        this.username = username;
        this.password = null;
    }

    public void setIsLogin(boolean isLogin) {
        isLogin = isLogin;
    }

    private boolean checkIsLogin(){
        return false;
    }

    public void login(MessageCallBack messageCallBack){
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), mediaType);
        Request request = new Request.Builder()
                .url(Tools.DOMAIN + "/api/login")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                messageCallBack.onFailure(e.toString());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body()!=null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        if("success".equals(status)){
                            messageCallBack.onSuccess(message);
                        }
                        else{
                            messageCallBack.onFailure(message);
                        }
                    } catch (JSONException e) {
                        messageCallBack.onFailure(e.toString());
                    }
                }
                else{
                    messageCallBack.onFailure("登录失败！");
                }
            }
        });
    }

    public void register(MessageCallBack messageCallBack){
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("username", username);
            jsonObject.put("password", password);
        } catch (Exception e) {
            messageCallBack.onFailure(e.toString());
        }
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), mediaType);
        Request request = new Request.Builder()
                .url(Tools.DOMAIN + "/api/register")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                messageCallBack.onFailure(e.toString());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful() && response.body()!= null){
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        if("success".equals(status)){
                            messageCallBack.onSuccess(message);
                        }
                        else{
                            messageCallBack.onFailure(message);
                        }
                    } catch (Exception e) {
                        messageCallBack.onFailure(e.toString());
                    }
                }
            }
        });
    }
}
