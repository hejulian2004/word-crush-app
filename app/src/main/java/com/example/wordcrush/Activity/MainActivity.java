package com.example.wordcrush.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.wordcrush.R;
import com.example.wordcrush.Server.AccountServer;
import com.example.wordcrush.Tools.MessageCallBack;
import com.example.wordcrush.Tools.Tools;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        username:
        loginToken:
        */
        try{
//            FileInputStream fis = openFileInput("userFile.txt");
//            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
//            String line;
//            String username = null;
//            String loginToken = null;
//            while ((line = reader.readLine()) != null) {
//                if (line.startsWith("userName:")) {
//                    username = line.substring("userName:".length()).trim();
//                } else if (line.startsWith("loginToken:")) {
//                    loginToken = line.substring("loginToken:".length()).trim();
//                }
//            }
//            reader.close();
//            fis.close();
//            if(Instant.now().getEpochSecond() > 7*24*60*60 + Long.parseLong(loginToken)){
//                toLoginPage();
//                return;
//            }
//            Tools.username = username;
            SharedPreferences sp = getApplicationContext().getSharedPreferences("word-crush", Context.MODE_PRIVATE);
            String token = sp.getString("token", "");
            String username = sp.getString("username", "");
            String uid = sp.getString("uid", "");
            if(token.isEmpty()){
                toLoginPage();
                return;
            }
            else{
                AccountServer.getInstance().checkToken(token, new MessageCallBack() {
                    @Override
                    public void onSuccess(String result) {
                        Tools.sendLog("token验证成功！");
                        Tools.username = username;
                        Tools.uid = uid;
                        Tools.token = token;
                    }

                    @Override
                    public void onFailure(String e) {
                        Tools.sendLog("token验证失败！" + e);
                        Tools.token = "";
                        Tools.uid = "";
                        Tools.username = "";
                        toLoginPage();
                    }
                });
            }
        } catch (Exception e){
            toLoginPage();
            return;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 手动获取 NavHostFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        NavHostFragment navHostFragment = (NavHostFragment) fragmentManager.findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            // 将 BottomNavigationView 与 NavController 绑定
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

    }

    @Override
    public void onClick(View v) {

    }

    public void toLoginPage(){
        Tools.sendLog("用户未登录");
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}