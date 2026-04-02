package com.example.wordcrush.Database.GameRecord;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "GAME_RECORD")
public class GameRecordEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int gameType;
    private int score;
    private String time;
    private String username;
    private List<String> learnedWords;

    public GameRecordEntity() {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getLearnedWords() {
        return learnedWords;
    }

    public void setLearnedWords(List<String> learnedWords) {
        this.learnedWords = learnedWords;
    }
}
