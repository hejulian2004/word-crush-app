package com.example.wordcrush.Ranking;

public class Ranking {
    private int rankingNumber, rankingScorer;
    private String rankingName, rankingTime;

    public Ranking(String rankingName, int rankingScore, String rankingTime){
        this.rankingName = rankingName;
        this.rankingScorer = rankingScore;
        this.rankingTime = rankingTime;
    }

    public void setRankingNumber(int rankingNumber) {
        this.rankingNumber = rankingNumber;
    }

    public int getRankingNumber() {
        return rankingNumber;
    }

    public void setRankingScorer(int rankingScorer) {
        this.rankingScorer = rankingScorer;
    }

    public int getRankingScorer() {
        return rankingScorer;
    }

    public String getRankingTime() {
        return rankingTime;
    }

    public void setRankingTime(String rankingTime) {
        this.rankingTime = rankingTime;
    }

    public void setRankingName(String rankingName) {
        this.rankingName = rankingName;
    }

    public String getRankingName() {
        return rankingName;
    }
}
