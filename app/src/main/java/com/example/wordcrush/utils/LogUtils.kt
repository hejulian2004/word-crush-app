package com.example.wordcrush.utils

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity

object LogUtils {
    private const val TAG = "WordCrush"
    private const val IS_DEBUG = true

    fun d(message: String) {
        if (IS_DEBUG) {
            Log.d(TAG, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (IS_DEBUG) {
            if (throwable != null) {
                Log.e(TAG, message, throwable)
            } else {
                Log.e(TAG, message)
            }
        }
    }

    fun i(message: String) {
        if (IS_DEBUG) {
            Log.i(TAG, message)
        }
    }

    fun w(message: String) {
        if (IS_DEBUG) {
            Log.w(TAG, message)
        }
    }

    fun toast(message: String, activity: FragmentActivity?) {
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun longToast(message: String, activity: FragmentActivity?) {
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
        }
    }
}
