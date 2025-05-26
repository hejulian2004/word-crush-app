package com.example.wordcrush.GameRecord;

import com.example.wordcrush.Database.GameRecordDatabase.GameRecordEntity;
import com.example.wordcrush.Word.Word;

import java.util.List;
public class GameRecord {
    private int gameType, score;
    private String time;

    private List<String> learnedWords;

    public GameRecord(){}

    public GameRecord(int gameType, int score, String time, List<String> learnedWords){
        this.gameType = gameType;
        this.score = score;
        this.time = time;
        this.learnedWords = learnedWords;
    }

    public GameRecord(GameRecordEntity gameRecord){
        this.gameType = gameRecord.getGameType();
        this.score = gameRecord.getScore();
        this.time = gameRecord.getTime();
        this.learnedWords = gameRecord.getLearnedWords();
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<String> getLearnedWords() {
        return learnedWords;
    }

    public void setLearnedWords(List<String> learnedWords) {
        this.learnedWords = learnedWords;
    }
}
