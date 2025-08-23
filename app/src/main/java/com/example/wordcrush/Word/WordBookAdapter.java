package com.example.wordcrush.Word;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wordcrush.R;
import com.example.wordcrush.Server.AudioServer;
import com.example.wordcrush.Server.WordServer;
import com.example.wordcrush.Tools.MessageCallBack;
import com.example.wordcrush.Tools.Tools;

import java.util.List;

public class WordBookAdapter extends RecyclerView.Adapter<WordBookAdapter.WordBookViewHolder> {
    private Context context;
    private List<Word> words;
    public WordBookAdapter(Context context, List<Word> words){
        this.context = context;
        this.words = words;
    }

    @NonNull
    @Override
    public WordBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_word, parent, false);
        return new WordBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordBookViewHolder holder, int position) {
        Word word = words.get(position);
        String id = position + 1 + ".";
        holder.idText.setText(id);
        holder.englishText.setText(word.getEnglish());
        holder.pronunciationText.setText(word.getPronunciation());
        holder.chineseText.setText(word.getChinese());
        if(word.getIsMaster()){
            holder.btnMastered.setBackgroundColor(0xFF4CAF50);
            holder.btnNotMastered.setBackgroundColor(0xFFDCDCDC);
        }
        else{
            holder.btnMastered.setBackgroundColor(0xFFDCDCDC);
            holder.btnNotMastered.setBackgroundColor(0xFFF44336);//#F44336
        }
        holder.labelUk.setOnClickListener(v ->{
            AudioServer.getInstance().getAudioServer(context, word.getEnglish(), 1);
        });
        holder.labelUs.setOnClickListener(v ->{
            AudioServer.getInstance().getAudioServer(context, word.getEnglish(), 0 );
        });
        holder.btnMastered.setOnClickListener(v ->{
            word.setIsMaster(true);
            notifyItemChanged(position);
            WordServer.getInstance().saveChange(context, word, new MessageCallBack() {
                @Override
                public void onSuccess(String result) {
                    Tools.sendLog("单词保存成功！");
                }

                @Override
                public void onFailure(String e) {

                }
            });
        });
        holder.btnNotMastered.setOnClickListener(v ->{
            word.setIsMaster(false);
            notifyItemChanged(position);
            WordServer.getInstance().saveChange(context, word, new MessageCallBack() {
                @Override
                public void onSuccess(String result) {
                    Tools.sendLog("单词保存成功！");
                }

                @Override
                public void onFailure(String e) {

                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return words.size();
    }


    static class WordBookViewHolder extends RecyclerView.ViewHolder{
        TextView idText, englishText, pronunciationText, chineseText, labelUk, labelUs;
        Button btnNotMastered, btnMastered;
        public WordBookViewHolder(@NonNull View itemView) {
            super(itemView);
            idText = itemView.findViewById(R.id.idText);
            englishText = itemView.findViewById(R.id.englishText);
            pronunciationText = itemView.findViewById(R.id.pronunciationText);
            chineseText = itemView.findViewById(R.id.chineseText);
            labelUk = itemView.findViewById(R.id.labelUk);
            labelUs = itemView.findViewById(R.id.labelUs);
            btnNotMastered = itemView.findViewById(R.id.btnNotMastered);
            btnMastered = itemView.findViewById(R.id.btnMastered);
        }
    }
}
