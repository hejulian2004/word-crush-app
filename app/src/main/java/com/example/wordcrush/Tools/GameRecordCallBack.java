package com.example.wordcrush.Tools;

import com.example.wordcrush.GameRecord.GameRecord;

import java.util.List;

public interface GameRecordCallBack {
    public void onSuccess(List<GameRecord> gameRecords);
    public void onFailed(String e);
}
