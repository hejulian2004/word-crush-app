package com.example.wordcrush.Tools;

import com.example.wordcrush.Word.Word;

import java.util.List;

public interface WordCallBack {
    void onSuccess(List<Word> words);

    void onFailure(String e);
}
