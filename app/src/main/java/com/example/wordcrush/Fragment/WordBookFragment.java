package com.example.wordcrush.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordcrush.R;
import com.example.wordcrush.Server.WordServer;
import com.example.wordcrush.Word.Word;
import com.example.wordcrush.Word.WordBookAdapter;
import com.example.wordcrush.Tools.WordCallBack;

import java.util.ArrayList;
import java.util.List;

public class WordBookFragment extends Fragment {

    private List<Word> words = new ArrayList<>();
    private RecyclerView recyclerView;
    private WordBookAdapter adapter;
    private EditText searchText;
    private Button searchBtn;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    RadioGroup radioGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_word_book, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.isMastered){
                    searchWords(true);
                }
                else{
                    searchWords(false);
                }
            }
        });
        searchText = view.findViewById(R.id.searchText);
        searchBtn = view.findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(words.isEmpty()){
                    return;
                }
                radioGroup.clearCheck();
                searchWords();
            }
        });
        recyclerView = view.findViewById(R.id.wordRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WordBookAdapter(requireContext(), words);
        recyclerView.setAdapter(adapter);

        loadWords();
    }

    private void searchWords(){
        String search = searchText.getText().toString().trim();
        WordServer.getInstance().searchWordsAsync(requireContext(), search, new WordCallBack() {
            @Override
            public void onSuccess(List<Word> searchWords) {
                mainHandler.post(() ->{
                    words.clear();
                    words.addAll(searchWords);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(String e) {
                mainHandler.post(() -> {
                    radioGroup.clearCheck();
                    loadWords();
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void searchWords(boolean isMastered){
        WordServer.getInstance().searchWordsAsync(requireContext(), isMastered, new WordCallBack() {
            @Override
            public void onSuccess(List<Word> searchWords) {
                mainHandler.post(() ->{
                    words.clear();
                    words.addAll(searchWords);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(String e) {
                mainHandler.post(() -> {
                    radioGroup.clearCheck();
                    loadWords();
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }
    private void loadWords() {
        WordServer.getInstance().loadWordsAsync(requireContext(), new WordCallBack() {
            @Override
            public void onSuccess(List<Word> loadWords) {
                mainHandler.post(() -> {
                    words.clear();
                    words.addAll(loadWords);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(String e) {
                mainHandler.post(() -> makeToast("加载失败：" + e));
            }
        });
    }

    private void makeToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        Log.d("Word-Crush", message);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
