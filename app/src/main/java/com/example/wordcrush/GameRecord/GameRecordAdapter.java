package com.example.wordcrush.GameRecord;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordcrush.R;

import java.util.List;

public class GameRecordAdapter extends RecyclerView.Adapter<GameRecordAdapter.GameRecordViewHolder> {
    private Context context;
    private List<GameRecord> gameRecords;

    public GameRecordAdapter(Context context, List<GameRecord> gameRecords){
        this.context = context;
        this.gameRecords = gameRecords;
    }


    @NonNull
    @Override
    public GameRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game_record, parent, false);
        return new GameRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameRecordViewHolder holder, int position) {
        GameRecord record = gameRecords.get(position);
        holder.recordNumber.setText(position + 1 + "");
        holder.recordType.setText(record.getGameType() == 0 ? "闯关模式" : "限时模式");
        holder.recordScore.setText("得分：" + record.getScore());
        holder.recordTime.setText("时间：" + record.getTime());
    }

    @Override
    public int getItemCount() {
        return gameRecords.size();
    }


    static class GameRecordViewHolder extends RecyclerView.ViewHolder{
        TextView recordNumber, recordType, recordScore, recordTime;
        public GameRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            recordNumber = itemView.findViewById(R.id.recordNumber);
            recordType = itemView.findViewById(R.id.recordType);
            recordScore = itemView.findViewById(R.id.recordScore);
            recordTime = itemView.findViewById(R.id.recordTime);
        }
    }
}
