package com.example.wordcrush.Server;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.example.wordcrush.Database.AppDatabase;
import com.example.wordcrush.Database.GameRecord.GameRecordEntity;
import com.example.wordcrush.Database.GameRecord.GameTypeScore;
import com.example.wordcrush.GameRecord.GameRecord;
import com.example.wordcrush.Tools.GameRecordCallBack;
import com.example.wordcrush.Tools.MessageCallBack;
import com.example.wordcrush.Tools.MyCallBack;
import com.example.wordcrush.Tools.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    private static volatile GameRecordServer gameRecordServer;

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static GameRecordServer getInstance(){
        if(gameRecordServer == null){
            synchronized (GameRecordServer.class){
                if(gameRecordServer == null){
                    gameRecordServer = new GameRecordServer();
                }
            }
        }
        return gameRecordServer;
    }

    private GameRecordServer(){}


    public void getGameRecordsAsync(Context context, GameRecordCallBack callBack){
        executorService.submit(()->{
            try{
                List<GameRecordEntity> gameRecordEntities = AppDatabase.getDatabase(context).gameRecordDao().getAllGameRecords(Tools.username);
                List<GameRecord> gameRecords = new ArrayList<>();
                for(GameRecordEntity entity: gameRecordEntities){
                    gameRecords.add(new GameRecord(entity));
                }
                callBack.onSuccess(gameRecords);
            }catch (Exception e){
                callBack.onFailure(e.toString());
            }
        });
    }

    public void setGameRecordsAsync(Context context, GameRecord record, MessageCallBack callBack){
        executorService.submit(() ->{
            try{
                AppDatabase.getDatabase(context).gameRecordDao().insertGameRecord(new GameRecordEntity(record));
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

    public void setGameRecordsAsync(Context context, List<GameRecord> records, MessageCallBack callBack){
        executorService.submit(() ->{
            List<GameRecordEntity> entities = new ArrayList<>();
            for(GameRecord record : records){
                entities.add(new GameRecordEntity(record));
            }
            try{
                AppDatabase.getDatabase(context).gameRecordDao().insertAllGameRecord(entities);
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

    public void getTopNRecord(int gameType, int topN, MyCallBack callBack){
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("gameType", gameType);
            jsonObject.put("topN", topN);
        }catch (Exception e){
            e.printStackTrace();
        }
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), mediaType);
        Request request = new Request.Builder()
                .url(Tools.DOMAIN + "/api/getTopNRecord")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callBack.onFailure(new Bundle());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body()!=null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String status = jsonObject.getString("status");
                        if("success".equals(status)){
                            JSONArray jsonArray = jsonObject.getJSONArray("message");
                            ArrayList<String> recordList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject item = jsonArray.getJSONObject(i);
                                recordList.add(item.toString());
                            }
                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList("records", recordList);
                            callBack.onSuccess(bundle);
                        }
                        else{
                        }
                    } catch (JSONException e) {
                        Bundle bundle = new Bundle();
                        bundle.putString("e", e.toString());
                        callBack.onFailure(bundle);
                    }
                }
                else{
                    Bundle bundle = new Bundle();
                    bundle.putString("e", "获取排行榜失败！");
                    callBack.onFailure(bundle);
                }
            }
        });
    }

    public void getAllScore(Context context, MyCallBack callBack){
        executorService.submit(()->{
            List<GameTypeScore> gameTypeScores = AppDatabase.getDatabase(context).gameRecordDao().getGameTypeAndScore(Tools.username);
            int score0 = 0;
            int score1 = 0;
            for(GameTypeScore record: gameTypeScores){
                if(record.getGameType() == 0){
                    score0 += record.getScore();
                } else{
                    score1 += record.getScore();
                }
            }
            Bundle bundle = new Bundle();
            bundle.putInt("score0", score0);
            bundle.putInt("score1", score1);
            Tools.sendLog("挑战模式分数："+score0+"限时模式："+score1);
            callBack.onSuccess(bundle);
        });
    }

    public void deleteAllRecords(Context context){
        executorService.submit(()->{
            AppDatabase.getDatabase(context).gameRecordDao().deleteAllRecords();
        });
    }

    public void insertAllRecords(Context context, List<GameRecordEntity> gameRecordEntities, MessageCallBack callBack){
        executorService.submit(()->{
            AppDatabase.getDatabase(context).gameRecordDao().insertAllRecords(gameRecordEntities);
            callBack.onSuccess("所有游戏记录保存成功！");
        });
    }
    public void deleteRecordByGameTypeScoreTime(Context context, int gameType, int score, String time, MessageCallBack callBack){
        executorService.submit(()->{
            int count = AppDatabase.getDatabase(context).gameRecordDao().deleteRecordByGameTypeScoreTime(gameType, score, time);
            if(count == 0){
                callBack.onFailure("游戏记录删除失败！");
            } else{
                deleteRecordFromCloud(gameType, score, time, new MessageCallBack() {
                    @Override
                    public void onSuccess(String result) {
                        callBack.onSuccess(result);
                    }

                    @Override
                    public void onFailure(String e) {
                        callBack.onFailure(e);
                    }
                });
            }
        });
    }

    public void deleteRecordFromCloud(int gameType, int score, String time, MessageCallBack messageCallBack){
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", Tools.username);
            jsonObject.put("gameType", gameType);
            jsonObject.put("score", score);
            jsonObject.put("time", time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), mediaType);
        Request request = new Request.Builder()
                .url(Tools.DOMAIN + "/api/deleteGameRecord")
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
                    messageCallBack.onFailure("游戏记录云端删除失败！");
                }
            }
        });
    }

    public void getAllRecordFromCloud(GameRecordCallBack callBack){
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", Tools.username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), mediaType);
        Request request = new Request.Builder()
                .url(Tools.DOMAIN + "/api/getAllGameRecord")
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
                            JSONArray jsonArray = jsonObject.getJSONArray("message");
                            List<GameRecord> gameRecords = new ArrayList<>();
                            for(int i=0; i< jsonArray.length(); i++){
                                JSONObject record = jsonArray.getJSONObject(i);
                                JSONArray wordsArray = record.getJSONArray("learnedWords");
                                List<String> learnedWords = new ArrayList<>();
                                for (int j = 0; j < wordsArray.length(); j++) {
                                    learnedWords.add(wordsArray.getString(j));
                                }
                                gameRecords.add(new GameRecord(
                                        record.getInt("gameType"),
                                        record.getInt("score"),
                                        record.getString("time"),
                                        learnedWords
                                ));
                            }
                            if(gameRecords.isEmpty()) {
                                callBack.onFailure("暂无数据！");
                            } else{
                                callBack.onSuccess(gameRecords);
                            }
                        }
                        else{
                            callBack.onFailure(message);
                        }
                    } catch (JSONException e) {
                        callBack.onFailure(e.toString());
                    }
                }
                else{
                    callBack.onFailure("游戏记录云端获取失败！");
                }
            }
        });
    }
}
