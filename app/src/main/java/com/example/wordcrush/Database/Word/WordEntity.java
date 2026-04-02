package com.example.wordcrush.Database.Word;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "WORDS")
public class WordEntity {
    @PrimaryKey
    private int id;
    private String english;
    private String pronunciation;
    private String chinese;
    private boolean isMaster;
    private int masterCount;

    public WordEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getChinese() {
        return chinese;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public int getMasterCount() {
        return masterCount;
    }

    public void setMasterCount(int masterCount) {
        this.masterCount = masterCount;
    }
}
