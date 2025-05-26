package com.example.wordcrush.Ranking;

public class Ranking {
    private int rankingNumber, rankingScorer, rankingHeart, rankingAvatar;
    private String rankingName;
    public Ranking(int rankingNumber, int rankingAvatar, String rankingName, int rankingScorer, int rankingHeart){
        this.rankingNumber = rankingNumber;
        this.rankingAvatar = rankingAvatar;
        this.rankingName = rankingName;
        this.rankingScorer = rankingScorer;
        this.rankingHeart = rankingHeart;
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

    public void setRankingHeart(int rankingHeart) {
        this.rankingHeart = rankingHeart;
    }

    public int getRankingHeart() {
        return rankingHeart;
    }

    public void setRankingAvatar(int rankingAvatar) {
        this.rankingAvatar = rankingAvatar;
    }

    public int getRankingAvatar() {
        return rankingAvatar;
    }

    public void setRankingName(String rankingName) {
        this.rankingName = rankingName;
    }

    public String getRankingName() {
        return rankingName;
    }
}
