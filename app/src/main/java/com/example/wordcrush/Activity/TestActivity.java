package com.example.wordcrush.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordcrush.R;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        findViewById(R.id.btn1).setOnClickListener(this);//主页
        findViewById(R.id.btn2).setOnClickListener(this);//登录
        findViewById(R.id.btn3).setOnClickListener(this);//注册
        findViewById(R.id.btn4).setOnClickListener(this);//生词本
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn1){
            Intent intent = new Intent(TestActivity.this, IndexActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.btn2){
            Intent intent = new Intent(TestActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.btn3){
            Intent intent = new Intent(TestActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.btn4){
            Intent intent = new Intent(TestActivity.this, WordBookActivity.class);
            startActivity(intent);
        }
    }
}
