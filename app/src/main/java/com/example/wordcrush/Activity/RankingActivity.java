package com.example.wordcrush.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordcrush.Database.AppDatabase;
import com.example.wordcrush.R;
import com.example.wordcrush.Ranking.Ranking;
import com.example.wordcrush.Ranking.RankingAdapter;
import com.example.wordcrush.Server.GameRecordServer;
import com.example.wordcrush.Tools.MyCallBack;
import com.example.wordcrush.Tools.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity implements View.OnClickListener {
    List<Ranking>rankings = new ArrayList<>();
    GameRecordServer gameRecordServerr;
    private AppDatabase appDatabase;
    TextView rankingTitleText;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        appDatabase = AppDatabase.getDatabase(this);
        gameRecordServerr = new GameRecordServer(this);
        Intent intent= getIntent();
        int gameType = intent.getIntExtra("gameType", 0);
        rankingTitleText = findViewById(R.id.rankingTitleText);
        if(gameType == 0){
            rankingTitleText.setText("闯关模式排行榜");
        } else{
            rankingTitleText.setText("限时模式排行榜");
        }
        gameRecordServerr.getTopNRecord(gameType, 50, new MyCallBack() {
            @Override
            public void onSuccess(Bundle bundle) {
                ArrayList<String> recordsJson = bundle.getStringArrayList("records");
                if (recordsJson != null) {
                    List<Ranking>tmp = new ArrayList<>();
                    for (String recordJson : recordsJson) {
                        try {
                            JSONObject obj = new JSONObject(recordJson);
                            String username = obj.getString("username");
                            int score = obj.getInt("score");
                            String time = obj.getString("time");
                            tmp.add(new Ranking(username, score, time));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    rankings.clear();
                    rankings.addAll(tmp);
                    runOnUiThread(()->{
                        if(rankings.isEmpty()){
                            Tools.toast("暂无记录！", RankingActivity.this);
                        }
                        RecyclerView recyclerView = findViewById(R.id.rankingRecyclerView);
                        recyclerView.setLayoutManager(new LinearLayoutManager(RankingActivity.this));
                        RankingAdapter adapter = new RankingAdapter(RankingActivity.this, rankings);
                        recyclerView.setAdapter(adapter);
                    });
                }
            }

            @Override
            public void onFailure(Bundle bundle) {

            }
        });
        RecyclerView recyclerView = findViewById(R.id.rankingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(RankingActivity.this));
        RankingAdapter adapter = new RankingAdapter(RankingActivity.this, rankings);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {

    }
}
