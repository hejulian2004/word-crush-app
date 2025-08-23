package com.example.wordcrush.Server;

import android.content.Context;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.os.Handler;
import android.os.Looper;

public class AudioServer {
    private static MediaPlayer mediaPlayer;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private AudioServer() {}

    private static volatile AudioServer audioServer;

    public static AudioServer getInstance(){
        if(audioServer == null){
            synchronized (AudioServer.class){
                if(audioServer == null){
                    audioServer = new AudioServer();
                }
            }
        }
        return audioServer;
    }

    public void getAudioServer(Context context, String word, int type) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://dict.youdao.com/dictvoice?type=" + type + "&audio=" + word)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    byte[] audioBytes = response.body().bytes();
                    // 切换到主线程播放音频
                    mainHandler.post(() -> playAudio(context, audioBytes));
                }
            }
        });
    }

    private void playAudio(Context context, byte[] audioData) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            // 创建临时文件保存音频数据
            File tempFile = File.createTempFile("audio", ".mp3", context.getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(audioData);
            fos.close();

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempFile.getAbsolutePath());
            mediaPlayer.setOnPreparedListener(mp -> mp.start());
            mediaPlayer.setOnCompletionListener(mp -> {
                // 播放完成后释放资源并删除临时文件
                mp.release();
                mediaPlayer = null;
                tempFile.delete();
            });
            mediaPlayer.prepareAsync(); // 异步准备

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

