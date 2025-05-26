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

public class AudioServer {
    private Context context;

    public AudioServer(Context context) {
        this.context = context;
    }
    public void getAudioServer(String word, int type){
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
                    playAudio(audioBytes);
                }
            }
        });
    }
    private void playAudio(byte[] audioData) {
        try {
            // 创建临时文件来保存音频数据
            File tempFile = File.createTempFile("audio", ".mp3", context.getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(audioData);
            fos.close();
            // 使用 MediaPlayer 播放音频
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
