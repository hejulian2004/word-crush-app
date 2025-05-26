package com.example.wordcrush.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordcrush.Tools.MessageCallBack;
import com.example.wordcrush.Tools.Tools;
import com.example.wordcrush.Server.AccountServer;
import com.example.wordcrush.R;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText usernameEditText;
    EditText passwordEditText;
    Button loginButton;
    Button registerButton;
    TextView errorTextView;

    ActivityResultLauncher<Intent> registerLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        errorTextView = findViewById(R.id.errorTextView);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
        registerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>(){
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if( result.getResultCode() == Tools.REGISTER_RESULT){
                            Intent data = result.getData();
                            if(data != null){
                                String tmpUsername = data.getStringExtra("username");
                                String tmpPassword = data.getStringExtra("password");
                                if(tmpUsername != null){
                                    usernameEditText.setText(tmpUsername);
                                }
                                if(tmpPassword != null){
                                    passwordEditText.setText(tmpPassword);
                                }
                                errorTextView.setVisibility(View.GONE);
                            }
                        }
                    }
                }
        );
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
            accountServer.login(new MessageCallBack() {
                @Override
                public void onSuccess(String result) {
                    //accountServer.setIsLogin(true);
                    makeToast(result);
                    try{
                        String filename = "userFile.txt";
                        String content = "userName:" + username + "\n"
                                        + "loginToken:" + Instant.now().getEpochSecond();
                        try(FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE)) {
                            fos.write(content.getBytes(StandardCharsets.UTF_8));
                        }
                    } catch (Exception e){
                        Tools.sendLog("userFile.txt写入失败！");
                    }
                    runOnUiThread(()->{
                        errorTextView.setVisibility(View.GONE);
                    });
                    Tools.username = username;
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
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
            registerLauncher.launch(intent);
        }
    }
}