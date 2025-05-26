package com.example.wordcrush.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordcrush.Database.AppDatabase;
import com.example.wordcrush.GameRecord.GameRecord;
import com.example.wordcrush.GameRecord.GameRecordAdapter;
import com.example.wordcrush.R;
import com.example.wordcrush.Server.GameRecordServer;
import com.example.wordcrush.Tools.GameRecordCallBack;

import java.util.ArrayList;
import java.util.List;

public class GameRecordActivity extends AppCompatActivity implements View.OnClickListener{
    List<GameRecord> gameRecords;

    GameRecordAdapter adapter;
    GameRecordServer gameRecordServer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_record);
        gameRecordServer = new GameRecordServer(this);
        gameRecords = new ArrayList<>();
        gameRecordServer.getGameRecordsAsync(new GameRecordCallBack() {
            @Override
            public void onSuccess(List<GameRecord> gr) {
                gameRecords.clear();
                gameRecords.addAll(gr);
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            }

            @Override
            public void onFailed(String e) {

            }
        });
        gameRecords.add(new GameRecord(0, 1, "这是时间", null));
        RecyclerView recyclerView = findViewById(R.id.rankingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(GameRecordActivity.this));
        adapter = new GameRecordAdapter(GameRecordActivity.this, gameRecords);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {

    }
}
