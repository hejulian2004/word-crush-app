package com.example.wordcrush.Tools;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Tools {
    //public static String DOMAIN = "http://192.168.201.21:5000";
    public static String DOMAIN = "http://hejulian.cn:5000";
    public static int REGISTER_RESULT = 0;

    public static void sendLog(String msg){
        Log.d("word-crush", msg);
    }

    public static void toast(String msg, Context context){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        sendLog(msg);
    }

    public static String username = "";

    public static String avatarUrl= "";
}
