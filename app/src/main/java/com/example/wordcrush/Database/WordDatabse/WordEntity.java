package com.example.wordcrush.Database.WordDatabse;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.wordcrush.Word.Word;

@Entity(tableName = "WORDS")
public class WordEntity {
    @PrimaryKey
    private int id;

    private String english, pronunciation, chinese;
    private boolean isMaster;
    private int masterCount;

    public WordEntity() {}

    public WordEntity(Word word){
        this.id = word.getId();
        this.english = word.getEnglish();
        this.pronunciation = word.getPronunciation();
        this.chinese = word.getChinese();
        this.isMaster = word.getIsMaster();
        this.masterCount = word.getMasterCount();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChinese() {
        return chinese;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
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
