package com.example.wordcrush.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wordcrush.Activity.RankingActivity;
import com.example.wordcrush.GameRecord.GameRecord;
import com.example.wordcrush.R;
import com.example.wordcrush.Server.AudioServer;
import com.example.wordcrush.Server.GameRecordServer;
import com.example.wordcrush.Server.WordServer;
import com.example.wordcrush.Tools.MessageCallBack;
import com.example.wordcrush.Tools.Tools;
import com.example.wordcrush.Word.Word;
import com.example.wordcrush.Tools.WordCallBack;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BreakthroughFragment extends Fragment implements View.OnClickListener{

    private AudioServer audioServer;
    private WordServer wordServer;

    private GameRecordServer gameRecordServer;
    private List<Word> words;

    private List<String> learnedWords;
    private boolean[] isSelected;
    private int[] match;
    private boolean [] isEnglish;
    private Button[] btn;
    private ImageView[] heart;
    private int heartCount;
    private int lastIndex = -1;
    private TextView scoreText;
    private int score;
    private TextView rankingBtn;
    private int wordIndex = 0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_breakthrough, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        audioServer = new AudioServer(requireContext());
        words = new ArrayList<>();
        wordServer = new WordServer(requireContext());
        gameRecordServer = new GameRecordServer(requireContext());
        rankingBtn = view.findViewById(R.id.rankingBtn);
        rankingBtn.setOnClickListener(this);
        getScoreText(view);
        getBtnAndSetListener(view);
        getHeart(view);

        getWords();
    }

    private void getWords(){
        wordServer.loadWordsAsync(new WordCallBack() {
            @Override
            public void onSuccess(List<Word> loadWords) {
                words.clear();
                words.addAll(loadWords);
                Tools.sendLog("单词获取成功！");
                Collections.shuffle(words);//随机打乱
                // 游戏开始，初始化
                requireActivity().runOnUiThread(() -> setDefault());
            }

            @Override
            public void onFailure(String e) {
                Tools.sendLog("单词获取失败！");
            }
        });
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.rankingBtn){
            Intent intent = new Intent(requireActivity(), RankingActivity.class);
            startActivity(intent);
            return;
        }
        int index = getIndex(v);
        if(lastIndex == -1){//如果是第一次选
            isSelected[index] = true;
            btn[index].setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
            lastIndex = index;
        } else if(index == lastIndex){//取消选择
            isSelected[index] = false;
            setBtnColorGRAY(index);
            lastIndex = -1;
        } else if(match[lastIndex] == index){//成功配对
            btn[lastIndex].setVisibility(View.GONE);
            btn[index].setVisibility(View.GONE);
            String english;
            if (isEnglish[lastIndex]) {
                english = btn[lastIndex].getText().toString();
                audioServer.getAudioServer(english, 1);
            } else {
                english = btn[index].getText().toString();
                audioServer.getAudioServer(english, 1);

            }
            learnedWords.add(english);
            wordServer.setMaster(btn[lastIndex].getText().toString(), new MessageCallBack() {
                @Override
                public void onSuccess(String result) {
                    Tools.sendLog(result);
                }

                @Override
                public void onFailure(String e) {
                    Tools.sendLog(e);
                }
            });
            lastIndex = -1;
            increaseScore();
            if(score%6==0){
                increaseHeart();
                goOnGame();
            }
        } else if(isEnglish[lastIndex] == isEnglish[index]){//二者同为英文或者同为中文，选择无效
            setBtnColorGRAY(lastIndex);
            setBtnColorGRAY(index);
            lastIndex = -1;
        } else{//配对错误
            setBtnColorGRAY(lastIndex);
            lastIndex = -1;
            decreaseHeart();
        }
    }

    private void setWord(){
        List<Word> word_tmp = new ArrayList<>();
        for(int i=0; i<6; i++){
            if(words.get(wordIndex).getIsMaster()){//如果已经学会了就跳过
                i--;
                continue;
            }
            word_tmp.add(words.get(wordIndex++));
            wordIndex = wordIndex%(words.size());
        }
        List<Integer>index_tmp = new ArrayList<>();
        for(int i=0; i<12 ;i++){
            index_tmp.add(i);
        }
        Collections.shuffle(index_tmp);
        for(int i=0; i<6; i++){
            btn[index_tmp.get(i)].setText(word_tmp.get(i).getEnglish());
            btn[index_tmp.get(i+6)].setText(word_tmp.get(i).getChinese());
            match[index_tmp.get(i)] = index_tmp.get(i+6);
            match[index_tmp.get(i+6)] = index_tmp.get(i);
            isEnglish[index_tmp.get(i)] = true;
            isEnglish[index_tmp.get(i+6)] = false;
        }
    }
    private void goOnGame(){
        setWord();
        setBtnVisibility();
        setBtnColor();
    }
    private void gameOver(){
        saveRecord();
        new AlertDialog.Builder(getContext())
                .setTitle("游戏结束！")
                .setMessage("您的得分是：" + score)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartGame();
                    }
                })
                .show();
    }

    private void saveRecord(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");
        String time = now.format(formatter);
        gameRecordServer.setGameRecordsAsync(new GameRecord(0, score, time, learnedWords), new MessageCallBack() {
            @Override
            public void onSuccess(String result) {
                Tools.sendLog(result);
            }

            @Override
            public void onFailure(String e) {
                Tools.sendLog(e);
            }
        });
    }

    private void restartGame(){
        //重新获取单词
        getWords();
        setDefault();
    }

    private void setHeart(){
        Tools.sendLog("heartCount:"+ heartCount);
        for (int i=0; i<heartCount ; i++){
            heart[i].setImageResource(R.drawable.icon_red_heart);
        }
        for (int i=heartCount; i<5 ;i++){
            heart[i].setImageResource(R.drawable.icon_white_heart);
        }
    }

    private void  increaseScore(){
        score += 1;
        setScore();
    }

    private void setScore(){
        scoreText.setText(String.valueOf(score));
    }

    private void increaseHeart(){
        if(heartCount == 5){
            return;
        }
        heartCount += 1;
        setHeart();
    }
    private void decreaseHeart(){
        if(heartCount == 1){
            gameOver();
            return;
        }
        heartCount -= 1;
        setHeart();
    }

    private void setDefault(){
        learnedWords = new ArrayList<>();
        isSelected = new boolean[12];
        match = new int[12];
        isEnglish = new boolean[12];
        lastIndex = -1;
        score = 0;
        heartCount = 5;
        setScore();
        setWord();
        setHeart();
        setBtnColor();
        setBtnVisibility();
    }

    private  void setBtnVisibility(){
        for(int i=0 ;i<12; i++){
            btn[i].setVisibility(View.VISIBLE);
        }
    }
    private void  setBtnColorGRAY(int i){
        btn[i].setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
    }
    private void  setBtnColor(){
        for(int i=0; i<12; i++){
            btn[i].setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        }
    }
    private void getScoreText(View view){
        scoreText = view.findViewById(R.id.scoreText);
    }
    private void getHeart(View view){
        heart = new ImageView[5];
        heartCount = 5;
        heart[0] = view.findViewById(R.id.heart1);
        heart[1] = view.findViewById(R.id.heart2);
        heart[2] = view.findViewById(R.id.heart3);
        heart[3] = view.findViewById(R.id.heart4);
        heart[4] = view.findViewById(R.id.heart5);
    }

    private void getBtnAndSetListener(View view){
        btn = new Button[12];
        btn[0] = view.findViewById(R.id.btn1);
        btn[0].setOnClickListener(this);
        btn[1] = view.findViewById(R.id.btn2);
        btn[1].setOnClickListener(this);
        btn[2] = view.findViewById(R.id.btn3);
        btn[2].setOnClickListener(this);
        btn[3] = view.findViewById(R.id.btn4);
        btn[3].setOnClickListener(this);
        btn[4] = view.findViewById(R.id.btn5);
        btn[4].setOnClickListener(this);
        btn[5] = view.findViewById(R.id.btn6);
        btn[5].setOnClickListener(this);
        btn[6] = view.findViewById(R.id.btn7);
        btn[6].setOnClickListener(this);
        btn[7] = view.findViewById(R.id.btn8);
        btn[7].setOnClickListener(this);
        btn[8] = view.findViewById(R.id.btn9);
        btn[8].setOnClickListener(this);
        btn[9] = view.findViewById(R.id.btn10);
        btn[9].setOnClickListener(this);
        btn[10] = view.findViewById(R.id.btn11);
        btn[10].setOnClickListener(this);
        btn[11] = view.findViewById(R.id.btn12);
        btn[11].setOnClickListener(this);
    }

    private int getIndex(View v){
        if(v.getId() == R.id.btn1){
            return 0;
        } else if(v.getId() == R.id.btn2){
            return 1;
        } else if(v.getId() == R.id.btn3){
            return 2;
        } else if(v.getId() == R.id.btn4){
            return 3;
        } else if(v.getId() == R.id.btn5){
            return 4;
        } else if(v.getId() == R.id.btn6){
            return 5;
        } else if(v.getId() == R.id.btn7){
            return 6;
        } else if(v.getId() == R.id.btn8){
            return 7;
        } else if(v.getId() == R.id.btn9){
            return 8;
        } else if(v.getId() == R.id.btn10){
            return 9;
        } else if(v.getId() == R.id.btn11){
            return 10;
        } else if(v.getId() == R.id.btn12){
            return 11;
        }
        return -1;
    }
}
