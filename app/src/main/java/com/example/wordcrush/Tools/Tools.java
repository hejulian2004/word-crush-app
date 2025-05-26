package com.example.wordcrush.Tools;

import android.util.Log;

public class Tools {
    public static String DOMAIN = "http://192.168.201.21:5000";
    public static int REGISTER_RESULT = 0;

    public static void sendLog(String msg){
        Log.d("word-crush", msg);
    }

    public static String username = "";
}
