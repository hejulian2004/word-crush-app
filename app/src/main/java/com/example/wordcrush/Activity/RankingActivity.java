package com.example.wordcrush.Activity;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordcrush.R;
import com.example.wordcrush.Ranking.Ranking;
import com.example.wordcrush.Ranking.RankingAdapter;
import com.example.wordcrush.Word.WordBookAdapter;

import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity implements View.OnClickListener {
    List<Ranking>rankings = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        rankings.add(new Ranking(1, R.drawable.logo, "hjl1", 100, 5));
        rankings.add(new Ranking(2, R.drawable.logo, "hjl2", 90, 5));
        rankings.add(new Ranking(3, R.drawable.logo, "hjl3", 80, 5));
        rankings.add(new Ranking(4, R.drawable.logo, "hjl4", 70, 5));
        rankings.add(new Ranking(5, R.drawable.logo, "hjl5", 60, 5));
        rankings.add(new Ranking(6, R.drawable.logo, "hjl6", 50, 5));
        RecyclerView recyclerView = findViewById(R.id.rankingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(RankingActivity.this));
        RankingAdapter adapter = new RankingAdapter(RankingActivity.this, rankings);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {

    }
}
