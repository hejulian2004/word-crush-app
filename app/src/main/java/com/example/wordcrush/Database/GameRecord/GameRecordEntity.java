package com.example.wordcrush.Database.GameRecord;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.wordcrush.GameRecord.GameRecord;
import com.example.wordcrush.Tools.Tools;

import java.util.List;

@Entity(tableName = "GAME_RECORD")
public class GameRecordEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int gameType, score;
    private String time, username;

    private List<String> learnedWords;

    public GameRecordEntity(){}
    @Ignore
    public GameRecordEntity(int gameType, int score, String time, List<String> learnedWords){
        this.username = Tools.username;
        this.gameType = gameType;
        this.score = score;
        this.time = time;
        this.learnedWords = learnedWords;
    }

    @Ignore
    public GameRecordEntity(GameRecord gameRecord){
        this.username = Tools.username;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
