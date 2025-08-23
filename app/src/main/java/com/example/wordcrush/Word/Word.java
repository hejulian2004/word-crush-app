package com.example.wordcrush.Word;

import com.example.wordcrush.Database.Word.WordEntity;

public class Word {
    private String english, pronunciation, chinese;
    private int id, masterCount;
    private boolean isMaster = false;

    public Word(int id, String english, String pronunciation, String chinese, Boolean isMaster, int masterCount){
        this.id = id;
        this.english = english;
        this.pronunciation = pronunciation;
        this.chinese = chinese;
        this.isMaster = isMaster;
        this.masterCount = masterCount;
    }

    public Word(WordEntity wordEntity){
        this.id = wordEntity.getId();
        this.english = wordEntity.getEnglish();
        this.pronunciation = wordEntity.getPronunciation();
        this.chinese = wordEntity.getChinese();
        this.isMaster = wordEntity.isMaster();
        this.masterCount = wordEntity.getMasterCount();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
    }

    public String getEnglish() {
        return english;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public String getChinese() {
        return chinese;
    }

    public int getMasterCount() {
        return masterCount;
    }

    public void setMasterCount(int masterCount) {
        this.masterCount = masterCount;
    }

    public void setIsMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }

    public boolean getIsMaster(){
        return isMaster;
    }
}
