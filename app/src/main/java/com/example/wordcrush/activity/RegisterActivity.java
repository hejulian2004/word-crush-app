package com.example.wordcrush.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordcrush.Tools;
import com.example.wordcrush.server.AccountServer;
import com.example.wordcrush.MyCallBack;
import com.example.wordcrush.R;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText registerUsernameEditText;
    EditText registerPasswordEditText;
    EditText confirmPasswordEditText;
    Button confirmRegisterButton;
    Button backButton;
    TextView errorTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

//        Button btn1 = findViewById(R.id.btn1);
//        btn1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String word = "king";
//                AudioServer audioServer = new AudioServer(MainActivity.this);
//                audioServer.getAudioServer(word, 1);
//            }
//        });

        registerUsernameEditText = findViewById(R.id.registerUsernameEditText);
        registerPasswordEditText = findViewById(R.id.registerPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        errorTextView = findViewById(R.id.errorTextView);
        confirmRegisterButton = findViewById(R.id.confirmRegisterButton);
        confirmRegisterButton.setOnClickListener(this);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);
    }

    public void makeToast(String message){
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.confirmRegisterButton){
            Intent intent = new Intent();
            String username = registerUsernameEditText.getText().toString();
            String password = registerPasswordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
                runOnUiThread(()->{
                    errorTextView.setText("账号或密码不能为空！");
                    errorTextView.setVisibility(View.VISIBLE);
                });
                return;
            }
            if(!password.equals(confirmPassword)){
                runOnUiThread(()->{
                    errorTextView.setText("两次密码不一致！");
                    errorTextView.setVisibility(View.VISIBLE);
                });
                return;
            }
            runOnUiThread(()->{
                errorTextView.setVisibility(View.GONE);
            });
            AccountServer accountServer = new AccountServer(username, password);
            accountServer.register(new MyCallBack() {
                @Override
                public void onSuccess(String result) {
                    makeToast(result);
                    runOnUiThread(()->{
                        errorTextView.setText("三秒后自动跳转至登录界面、请稍后......");
                        errorTextView.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("password", password);
                            setResult(Tools.REGISTER_RESULT, intent);
                            finish();
                        }, 3000);
                    });
                }

                @Override
                public void onFailure(String e) {
                    makeToast(e);
                    runOnUiThread(()->{
                        errorTextView.setText(e);
                        errorTextView.setVisibility(View.VISIBLE);
                    });
                }
            });
        }

    }
}