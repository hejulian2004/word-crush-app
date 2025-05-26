package com.example.wordcrush.Database.GameRecordDatabase;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.wordcrush.Database.Converters;
import com.example.wordcrush.GameRecord.GameRecord;

import java.util.List;

@Entity(tableName = "GAME_RECORD")
public class GameRecordEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int gameType, score;
    private String time;

    private List<String> learnedWords;

    public GameRecordEntity(){}
    @Ignore
    public GameRecordEntity(int gameType, int score, String time, List<String> learnedWords){
        this.gameType = gameType;
        this.score = score;
        this.time = time;
        this.learnedWords = learnedWords;
    }

    @Ignore
    public GameRecordEntity(GameRecord gameRecord){
        this.gameType = gameRecord.getGameType();
        this.score = gameRecord.getScore();
        this.time = gameRecord.getTime();
        this.learnedWords = gameRecord.getLearnedWords();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
