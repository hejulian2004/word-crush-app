package com.example.wordcrush.Ranking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordcrush.R;
import com.example.wordcrush.Server.AudioServer;
import com.example.wordcrush.Word.Word;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {
    private Context context;
    private List<Ranking> rankings;

    public RankingAdapter(Context context, List<Ranking> rankings){
        this.context = context;
        this.rankings = rankings;
    }


    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ranking, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        Ranking ranking = rankings.get(position);
        holder.rankingNumber.setText(String.valueOf(ranking.getRankingNumber()));
        holder.rankingAvatar.setImageResource(ranking.getRankingAvatar());
        holder.rankingScore.setText("得分：" + ranking.getRankingScorer());
        holder.rankingHeart.setText("剩余生命值：" + ranking.getRankingHeart());
    }

    @Override
    public int getItemCount() {
        return rankings.size();
    }


    static class RankingViewHolder extends RecyclerView.ViewHolder{
        TextView rankingNumber, rankingName, rankingScore, rankingHeart;
        ImageView rankingAvatar;
        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            rankingNumber = itemView.findViewById(R.id.rankingNumber);
            rankingName = itemView.findViewById(R.id.rankingName);
            rankingScore = itemView.findViewById(R.id.rankingScore);
            rankingHeart = itemView.findViewById(R.id.rankingHeart);
            rankingAvatar = itemView.findViewById(R.id.rankingAvatar);
        }
    }
}
