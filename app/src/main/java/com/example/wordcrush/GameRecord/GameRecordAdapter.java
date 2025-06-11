package com.example.wordcrush.GameRecord;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordcrush.R;
import com.example.wordcrush.Server.GameRecordServer;
import com.example.wordcrush.Tools.MessageCallBack;
import com.example.wordcrush.Tools.Tools;

import java.util.List;

public class GameRecordAdapter extends RecyclerView.Adapter<GameRecordAdapter.GameRecordViewHolder> {
    private Context context;
    private List<GameRecord> gameRecords;

    private GameRecordServer gameRecordServer;

    public GameRecordAdapter(Context context, List<GameRecord> gameRecords){
        this.context = context;
        this.gameRecords = gameRecords;
        this.gameRecordServer = new GameRecordServer(context);
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

        holder.recordItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    GameRecord _record = gameRecords.get(currentPos);
                    StringBuilder message = new StringBuilder();
                    message.append("时间：" + _record.getTime() + "\n" + "学习单词：\n");
                    int id = 1;
                    for (String word : _record.getLearnedWords()) {
                        message.append(id++ + "." + word + "\n");
                    }

                    new AlertDialog.Builder(context)
                            .setTitle("游戏记录")
                            .setMessage(message)
                            .setPositiveButton("确定", null)
                            .show();
                }
            }
        });

        holder.recordItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("删除游戏记录")
                        .setMessage("确定删除此条游戏记录吗？")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int currentPos = holder.getAdapterPosition();
                                if (currentPos == RecyclerView.NO_POSITION){
                                    ((Activity) context).runOnUiThread(()->{
                                        Tools.toast("删除失败！请重试", context);
                                        notifyDataSetChanged();
                                    });
                                    return;
                                }
                                GameRecord _record = gameRecords.get(currentPos);
                                gameRecordServer.deleteRecordByGameTypeScoreTime(_record.getGameType(), _record.getScore(), _record.getTime(), new MessageCallBack() {
                                    @Override
                                    public void onSuccess(String result) {
                                        ((Activity) context).runOnUiThread(()->{
                                            gameRecords.remove(currentPos);
                                            notifyDataSetChanged();
                                            Tools.toast(result, context);
                                        });
                                    }

                                    @Override
                                    public void onFailure(String e) {
                                        ((Activity) context).runOnUiThread(()->{
                                            Tools.toast(e, context);
                                        });
                                    }
                                });
                            }
                        })
                        .setNegativeButton("取消",null)
                        .show();

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return gameRecords.size();
    }


    static class GameRecordViewHolder extends RecyclerView.ViewHolder{
        TextView recordNumber, recordType, recordScore, recordTime;
        LinearLayout recordItem;
        public GameRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            recordNumber = itemView.findViewById(R.id.recordNumber);
            recordType = itemView.findViewById(R.id.recordType);
            recordScore = itemView.findViewById(R.id.recordScore);
            recordTime = itemView.findViewById(R.id.recordTime);
            recordItem = itemView.findViewById(R.id.recordItem);
        }
    }
}
