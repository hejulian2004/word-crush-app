package com.example.wordcrush.Server;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.wordcrush.Database.AppDatabase;
import com.example.wordcrush.Database.GameRecordDatabase.GameRecordEntity;
import com.example.wordcrush.GameRecord.GameRecord;
import com.example.wordcrush.Tools.GameRecordCallBack;
import com.example.wordcrush.Tools.MessageCallBack;
import com.example.wordcrush.Tools.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GameRecordServer {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private AppDatabase appDatabase;
    private Context context;
    ExecutorService executorService;
    public GameRecordServer(Context context){
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        this.appDatabase = AppDatabase.getDatabase(context);
    }

    public void getGameRecordsAsync(GameRecordCallBack callBack){
        executorService.submit(()->{
            try{
                List<GameRecordEntity> gameRecordEntities = appDatabase.gameRecordDao().getAllGameRecords();
                List<GameRecord> gameRecords = new ArrayList<>();
                for(GameRecordEntity entity: gameRecordEntities){
                    gameRecords.add(new GameRecord(entity));
                }
                callBack.onSuccess(gameRecords);
            }catch (Exception e){
                callBack.onFailed(e.toString());
            }
        });
    }

    public void setGameRecordsAsync(GameRecord record, MessageCallBack callBack){
        executorService.submit(() ->{
            try{
                appDatabase.gameRecordDao().insertGameRecord(new GameRecordEntity(record));
                callBack.onSuccess("游戏记录插入成功！" + record);
                uploadCloud(record, new MessageCallBack() {
                    @Override
                    public void onSuccess(String result) {
                        Tools.sendLog(result);
                    }

                    @Override
                    public void onFailure(String e) {
                        Tools.sendLog(e);
                    }
                });
            } catch (Exception e){
                callBack.onFailure("游戏记录插入失败！" + record);
            }

        });
    }

    public void setGameRecordsAsync(List<GameRecord> records, MessageCallBack callBack){
        executorService.submit(() ->{
            List<GameRecordEntity> entities = new ArrayList<>();
            for(GameRecord record : records){
                entities.add(new GameRecordEntity(record));
            }
            try{
                appDatabase.gameRecordDao().insertAllGameRecord(entities);
                callBack.onSuccess("游戏记录插入成功！" + entities);
            } catch (Exception e){
                callBack.onFailure("游戏记录插入失败！" + entities);
            }

        });
    }

    public void uploadCloud(GameRecord record, MessageCallBack messageCallBack){
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", Tools.username);
            jsonObject.put("gameType", record.getGameType());
            jsonObject.put("score", record.getScore());
            jsonObject.put("time", record.getTime());
            JSONArray jsonArray = new JSONArray(record.getLearnedWords());
            jsonObject.put("learnedWords", jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), mediaType);
        Request request = new Request.Builder()
                .url(Tools.DOMAIN + "/api/addGameRecord")
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
                    messageCallBack.onFailure("游戏记录上传云端失败！");
                }
            }
        });
    }

    public void getTopNRecord(int n, GameRecordCallBack callBack){

    }
}
