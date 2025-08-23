package com.example.wordcrush.Database.GameRecord;

public class GameTypeScore {
    public int gameType;
    public int score;

    public GameTypeScore(int gameType, int score) {
        this.gameType = gameType;
        this.score = score;
    }

    public int getGameType() {
        return gameType;
    }

    public int getScore() {
        return score;
    }
}
