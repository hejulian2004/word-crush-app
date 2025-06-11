package com.example.wordcrush.Ranking;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wordcrush.R;
import com.example.wordcrush.Tools.Tools;

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
        holder.rankingName.setText(ranking.getRankingName());
        holder.rankingScore.setText("得分：" + ranking.getRankingScorer() );
        holder.rankingNumber.setText(position + 1 + "");
        holder.rankingTime.setText(ranking.getRankingTime());
        Glide.with(context)
                .load(Tools.DOMAIN +  "/static/avatars/" + ranking.getRankingName() + ".jpg?time=" + System.currentTimeMillis())
                .placeholder(R.drawable.default_avatar) // 默认头像
                .into(holder.rankingAvatar);
    }

    @Override
    public int getItemCount() {
        return rankings.size();
    }


    static class RankingViewHolder extends RecyclerView.ViewHolder{
        TextView rankingNumber, rankingName, rankingScore, rankingTime;
        ImageView rankingAvatar;
        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            rankingNumber = itemView.findViewById(R.id.rankingNumber);
            rankingName = itemView.findViewById(R.id.rankingName);
            rankingScore = itemView.findViewById(R.id.rankingScore);
            rankingTime = itemView.findViewById(R.id.rankingTime);
            rankingAvatar = itemView.findViewById(R.id.rankingAvatar);
        }
    }
}
