package com.example.wordcrush.Server;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.wordcrush.Tools.ApiResponse;
import com.example.wordcrush.Tools.MessageCallBack;
import com.example.wordcrush.Tools.MyCallBack;
import com.example.wordcrush.Tools.Tools;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccountServer {

    private static volatile AccountServer instance;

    private AccountServer(){}

    public static AccountServer getInstance(){
        if(instance == null){
            synchronized (AccountServer.class){
                if(instance == null){
                    instance = new AccountServer();
                }
            }
        }
        return instance;
    }

    public void login(String username, String password, MessageCallBack messageCallBack){
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
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
                .url(Tools.DOMAIN + Tools.loginUrl)
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
                    String responseBody = response.body().string();
                    Type type = new com.google.gson.reflect.TypeToken<ApiResponse<Map<String, String>>>() {}.getType();
                    ApiResponse<Map<String, String>> apiResponse = gson.fromJson(responseBody, type);
                    int code = apiResponse.getCode();
                    String msg = apiResponse.getMsg();
                    if(code == 200){
                        try {
                            Map<String, String> data = apiResponse.getData();
                            Tools.username = data.get("username");
                            Tools.uid = data.get("uid");
                            Tools.token = data.get("token");
                            Tools.sendLog("username:" + Tools.username + " uid:" + Tools.uid + " token:" + Tools.token );
                            messageCallBack.onSuccess(msg);
                        }
                        catch (Exception e){
                            messageCallBack.onFailure(e.toString());
                            e.printStackTrace();
                        }
                    }
                    else {
                        messageCallBack.onFailure(msg);
                    }
                }
                else{
                    messageCallBack.onFailure("网络请求错误！");
                }
            }
        });
    }

    public void checkToken(String token, MessageCallBack messageCallBack){
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        Request request = new Request.Builder()
                .url(Tools.DOMAIN + Tools.checkTokenUrl + "?token=" + token)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                messageCallBack.onFailure(e.toString());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body()!=null) {
                    String responseBody = response.body().string();
                    Type type = new com.google.gson.reflect.TypeToken<ApiResponse<Map<String, String>>>() {}.getType();
                    ApiResponse<Map<String, String>> apiResponse = gson.fromJson(responseBody, type);
                    int code = apiResponse.getCode();
                    String msg = apiResponse.getMsg();
                    if(code == 200){
                        try {
                            Map<String, String> data = apiResponse.getData();
                            Tools.username = data.get("username");
                            Tools.uid = data.get("uid");
                            Tools.token = data.get("token");
                            Tools.sendLog("username:" + Tools.username + " uid:" + Tools.uid + " token:" + Tools.token );
                            messageCallBack.onSuccess(msg);
                        }
                        catch (Exception e){
                            messageCallBack.onFailure(e.toString());
                            e.printStackTrace();
                        }
                    }
                    else {
                        messageCallBack.onFailure(msg);
                    }
                }
                else{
                    Tools.sendLog(response.toString());
                    messageCallBack.onFailure("网络请求错误！");
                }
            }
        });
    }

    public void register(String username, String password, MessageCallBack messageCallBack){
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

    public void uploadImageToServer(File imageFile, MessageCallBack callBack) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", Tools.username)
                .addFormDataPart("avatar", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                .build();

        Request request = new Request.Builder()
                .url(Tools.DOMAIN + "/api/uploadAvatar")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callBack.onFailure("头像上传失败"+e);
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
                            callBack.onSuccess(message);
                        }
                        else{
                            callBack.onFailure(message);
                        }
                    } catch (Exception e) {
                        callBack.onFailure(e.toString());
                    }
                }
            }
        });
    }

    public void changePassword(String oldPassword, String newPassword, MessageCallBack callBack){
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", Tools.username);
            jsonObject.put("oldPassword", oldPassword);
            jsonObject.put("newPassword", newPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), mediaType);
        Request request = new Request.Builder()
                .url(Tools.DOMAIN + "/api/changePassword")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callBack.onFailure(e.toString());
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
                            callBack.onSuccess(message);
                        }
                        else{
                            callBack.onFailure(message);
                        }
                    } catch (JSONException e) {
                        callBack.onFailure(e.toString());
                    }
                }
                else{
                    callBack.onFailure("修改密码失败！");
                }
            }
        });
    }
}
