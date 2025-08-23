package com.example.wordcrush.Tools;
public class ApiResponse<T> {
    private int code;
    private String msg;

    private T data;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
