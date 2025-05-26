//package com.example.wordcrush.Activity;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.wordcrush.Database.AppDatabase;
//import com.example.wordcrush.Database.WordDatabse.WordEntity;
//import com.example.wordcrush.R;
//import com.example.wordcrush.Server.WordLoatServer;
//import com.example.wordcrush.Word.Word;
//import com.example.wordcrush.Word.WordBookAdapter;
//import com.example.wordcrush.WordLoadCallBack;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class WordBookActivity extends AppCompatActivity implements View.OnClickListener {
//
//    private List<Word> words = new ArrayList<>();
//
//    private AppDatabase appDatabase;
//    private final WordLoatServer wordLoatServer = new WordLoatServer(WordBookActivity.this, "wordbook.csv");
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_word_book);
//
//        appDatabase = AppDatabase.getDatabase(WordBookActivity.this);
//
//        List<WordEntity> wordEntity = appDatabase.wordDao().getAllWords();
//        if(wordEntity.isEmpty()){//如果数据库中没有
//            makeToast("数据库中没有内容，单词加载中");
//            wordLoatServer.loadWordsAsync(new WordLoadCallBack() {
//                @Override
//                public void onSuccess(List<Word> loadWords) {
//                    words.clear();
//                    words.addAll(loadWords);
//                    List<WordEntity> tmp = new ArrayList<>();
//                    for(Word word : words){
//                        tmp.add(new WordEntity(word));
//                    }
//                    appDatabase.wordDao().insertAll(tmp);
//                    runOnUiThread(()->{
//                        RecyclerView recyclerView = findViewById(R.id.wordRecyclerView);
//                        recyclerView.setLayoutManager(new LinearLayoutManager(WordBookActivity.this));
//                        WordBookAdapter adapter = new WordBookAdapter(WordBookActivity.this, words);
//                        recyclerView.setAdapter(adapter);
//                    });
//                }
//                @Override
//                public void onFailure(String e) {
//                    makeToast(e);
//                }
//            });
//        }
//        else{
//            for(WordEntity word : wordEntity){
//                words.add(new Word(word));
//            }
//            RecyclerView recyclerView = findViewById(R.id.wordRecyclerView);
//            recyclerView.setLayoutManager(new LinearLayoutManager(WordBookActivity.this));
//            WordBookAdapter adapter = new WordBookAdapter(WordBookActivity.this, words);
//            recyclerView.setAdapter(adapter);
//        }
//    }
//
//    public void makeToast(String message){
//        runOnUiThread(() -> {
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//        });
//    }
//
//    @Override
//    public void onClick(View v) {
//
//    }
//}

package com.example.wordcrush.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordcrush.Database.AppDatabase;
import com.example.wordcrush.Database.WordDatabse.WordEntity;
import com.example.wordcrush.R;
import com.example.wordcrush.Server.WordServer;
import com.example.wordcrush.Word.Word;
import com.example.wordcrush.Word.WordBookAdapter;
import com.example.wordcrush.Tools.WordCallBack;

import java.util.ArrayList;
import java.util.List;

public class WordBookActivity extends AppCompatActivity implements View.OnClickListener {

    private List<Word> words = new ArrayList<>();
    private AppDatabase appDatabase;
    private final WordServer wordServer = new WordServer(WordBookActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_book);

        appDatabase = AppDatabase.getDatabase(WordBookActivity.this);

        // 使用异步任务来查询数据库
        new LoadWordsTask().execute();
    }

    // 异步加载数据库和CSV中的单词
    private class LoadWordsTask extends AsyncTask<Void, Void, List<WordEntity>> {

        @Override
        protected List<WordEntity> doInBackground(Void... voids) {
            return appDatabase.wordDao().getAllWords(); // 在后台线程查询数据库
        }

        @Override
        protected void onPostExecute(List<WordEntity> wordEntities) {
            super.onPostExecute(wordEntities);

            if (wordEntities.isEmpty()) { // 如果数据库中没有内容
                makeToast("数据库中没有内容，单词加载中");

                // 异步加载 CSV 数据
                wordServer.loadWordsAsync(new WordCallBack() {
                    @Override
                    public void onSuccess(List<Word> loadWords) {
                        words.clear();
                        words.addAll(loadWords);

                        // 将加载的单词保存到数据库中
                        List<WordEntity> tmp = new ArrayList<>();
                        for (Word word : words) {
                            tmp.add(new WordEntity(word));
                        }

                        // 在后台线程中插入数据
                        new InsertWordsTask().execute(tmp);
                    }

                    @Override
                    public void onFailure(String e) {
                        makeToast(e);
                    }
                });
            } else {
                words.clear();
                // 如果数据库中已有内容，直接加载
                for (WordEntity word : wordEntities) {
                    words.add(new Word(word));
                }

                // 更新UI，显示数据
                updateRecyclerView();
            }
        }
    }

    // 异步插入单词到数据库
    private class InsertWordsTask extends AsyncTask<List<WordEntity>, Void, Void> {

        @Override
        protected Void doInBackground(List<WordEntity>... lists) {
            appDatabase.wordDao().insertAll(lists[0]); // 在后台线程插入数据
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // 数据插入成功后，更新UI
            updateRecyclerView();
        }
    }

    // 更新 RecyclerView
    private void updateRecyclerView() {
        runOnUiThread(() -> {
            RecyclerView recyclerView = findViewById(R.id.wordRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(WordBookActivity.this));
            WordBookAdapter adapter = new WordBookAdapter(WordBookActivity.this, words);
            recyclerView.setAdapter(adapter);
        });
    }

    public void makeToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            segLog(message);
        });
    }

    public void segLog(String message){
        Log.d("Word-Crush", message);
    }

    @Override
    public void onClick(View v) {
        // 在这里处理点击事件
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveAllWords();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveAllWords();
    }

    private void saveAllWords() {
        new Thread(() -> {
            List<WordEntity> entities = new ArrayList<>();
            for (Word word : words) {
                entities.add(new WordEntity(word));
            }
            appDatabase.wordDao().updateAll(entities);
        }).start();

        makeToast("单词保存成功！");
    }
}

