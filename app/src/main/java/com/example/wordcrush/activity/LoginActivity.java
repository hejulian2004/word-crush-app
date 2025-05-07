package com.example.wordcrush.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordcrush.server.AccountServer;
import com.example.wordcrush.MyCallBack;
import com.example.wordcrush.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText usernameEditText;
    EditText passwordEditText;
    Button loginButton;
    Button registerButton;
    TextView errorTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

//        Button btn1 = findViewById(R.id.btn1);
//        btn1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String word = "king";
//                AudioServer audioServer = new AudioServer(MainActivity.this);
//                audioServer.getAudioServer(word, 1);
//            }
//        });

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Intent intent = getIntent();
        String tmpUsername = intent.getStringExtra("username");
        String tmpPassword = intent.getStringExtra("password");
        if(tmpUsername != null){
            usernameEditText.setText(tmpUsername);
        }
        if(tmpPassword != null){
            passwordEditText.setText(tmpPassword);
        }
        errorTextView = findViewById(R.id.errorTextView);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
    }

    public void makeToast(String message){
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.loginButton){
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            AccountServer accountServer = new AccountServer(username, password);
            accountServer.login(new MyCallBack() {
                @Override
                public void onSuccess(String result) {
                    accountServer.setIsLogin(true);
                    makeToast(result);
                    runOnUiThread(()->{
                        errorTextView.setVisibility(View.GONE);

                    });
                }

                @Override
                public void onFailure(String e) {
                    accountServer.setIsLogin(false);
                    makeToast(e);
                    runOnUiThread(()->{
                        errorTextView.setText(e);
                        errorTextView.setVisibility(View.VISIBLE);
                    });
                }
            });
        } else if (vid == R.id.registerButton){
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
    }
}