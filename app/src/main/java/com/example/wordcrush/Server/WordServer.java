package com.example.wordcrush.Server;

import android.content.Context;
import android.content.res.AssetManager;
import com.example.wordcrush.Database.AppDatabase;
import com.example.wordcrush.Database.WordDatabse.WordEntity;
import com.example.wordcrush.Tools.MessageCallBack;
import com.example.wordcrush.Tools.Tools;
import com.example.wordcrush.Tools.WordCallBack;
import com.example.wordcrush.Word.Word;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.opencsv.CSVReader;

public class WordServer {
    private static List<Word> cachedWords = new ArrayList<>(); // 所有实例共享
    private static boolean isLoading = false;
    private AppDatabase appDatabase;
    private Context context;
    private String fileName = "wordbook.csv";

    ExecutorService executorService;
    public WordServer(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        this.appDatabase = AppDatabase.getDatabase(context);
    }

    public void loadWordsAsync(WordCallBack wordCallBack) {
        executorService.submit(() -> {
            if(isLoading){
                wordCallBack.onSuccess(cachedWords);
                Tools.sendLog("缓存已有单词，从缓存加载");
                return;
            }
            List<WordEntity> wordEntities = appDatabase.wordDao().getAllWords();
            if(!wordEntities.isEmpty()){
                List<Word> words = new ArrayList<>();
                for(WordEntity word : wordEntities){
                    words.add(new Word(word));
                }
                wordCallBack.onSuccess(words);
                Tools.sendLog("数据库已有单词，从数据库加载");
                isLoading = true;
                cachedWords.clear();
                cachedWords.addAll(words);
                return;
            }
            AssetManager assetManager = context.getAssets();
            try (CSVReader reader = new CSVReader(new InputStreamReader(assetManager.open(fileName)))){
                List<String[]> lines = reader.readAll();
                List<Word> words = new ArrayList<>();
                List<WordEntity> wordEntityList = new ArrayList<>();
                int cnt = 0;
                for (String[] values : lines) {
                    if (values.length == 4) {
                        if(values[0].equals("序号")){
                            continue;
                        }
                        cnt++;
                        Word word = new Word(cnt, values[1], values[2], values[3], false, 0);
                        wordEntityList.add(new WordEntity(word));
                        words.add(word);
                    }
                }
                cachedWords.clear();
                cachedWords.addAll(words);
                isLoading = true;
                Tools.sendLog("从文件中加载单词");
                //executorService.execute(() ->{
                    appDatabase.wordDao().insertAll(wordEntityList);
                    Tools.sendLog("保存到数据库成功！");
                //});
                wordCallBack.onSuccess(words);
            } catch (Exception e) {
                wordCallBack.onFailure("文件读取失败！"+ e);
            }
        });
    }

    public void searchWordsAsync(String search, WordCallBack wordSearchCallBack){
        executorService.submit(() ->{
            List<WordEntity> wordEntities = appDatabase.wordDao().searchWords(search);
            if(wordEntities.isEmpty()){
                wordSearchCallBack.onFailure("未查询到信息！");
                Tools.sendLog("未查询到信息！");
            }
            List<Word> words = new ArrayList<>();
            for(WordEntity word : wordEntities){
                words.add(new Word(word));
            }
            wordSearchCallBack.onSuccess(words);
            Tools.sendLog("查询成功！");
        });
    }

    public void searchWordsAsync(Boolean isMastered, WordCallBack wordSearchCallBack){
        executorService.submit(() ->{
            List<WordEntity> wordEntities = appDatabase.wordDao().searchWords(isMastered);
            if(wordEntities.isEmpty()){
                wordSearchCallBack.onFailure("未查询到信息！");
                Tools.sendLog("未查询到信息！");
            }
            List<Word> words = new ArrayList<>();
            for(WordEntity word : wordEntities){
                words.add(new Word(word));
            }
            wordSearchCallBack.onSuccess(words);
            Tools.sendLog("查询成功！");
        });
    }

    public void saveChange(Word word, MessageCallBack messageCallBack){
        executorService.submit(()->{
           appDatabase.wordDao().update(new WordEntity(word));
           cachedWords.clear();
           isLoading = false;
           messageCallBack.onSuccess("保存成功！");
        });
    }

    public void setMaster(String english, MessageCallBack messageCallBack){
        executorService.submit(()->{
            WordEntity entity = appDatabase.wordDao().getWordByEnglish(english);//先查询到这个单词
            entity.setMaster(true);
            saveChange(new Word(entity), new MessageCallBack() {
                @Override
                public void onSuccess(String result) {
                    messageCallBack.onSuccess("单词修改为已掌握！" + entity.getEnglish());
                }

                @Override
                public void onFailure(String e) {
                    messageCallBack.onSuccess("修改失败！" + entity.getEnglish());
                }
            });
        });
    }

    public void setMaster(List<String> learnedWord){
        executorService.submit(()->{
            for(String english: learnedWord){
                setMaster(english, new MessageCallBack() {
                    @Override
                    public void onSuccess(String result) {
                        Tools.sendLog(english + "已修改为掌握");
                    }

                    @Override
                    public void onFailure(String e) {
                        Tools.sendLog(english + "修改为掌握-失败");
                    }
                });
            }
        });
    }

    public void setAllWordNotMaster(){
        executorService.submit(()->{
            List<WordEntity> entities = appDatabase.wordDao().getAllWords();
            for(WordEntity wordEntity: entities){
                wordEntity.setMaster(false);
            }
            appDatabase.wordDao().updateAll(entities);
            isLoading = false;
        });
    }
}
