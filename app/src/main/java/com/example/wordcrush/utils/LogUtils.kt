package com.example.wordcrush.utils

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.wordcrush.constants.AppConstants

object LogUtils {
    fun d(message: String) {
        if (AppConstants.Logging.IS_DEBUG) {
            Log.d(AppConstants.Logging.TAG, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (AppConstants.Logging.IS_DEBUG) {
            if (throwable != null) {
                Log.e(AppConstants.Logging.TAG, message, throwable)
            } else {
                Log.e(AppConstants.Logging.TAG, message)
            }
        }
    }

    fun i(message: String) {
        if (AppConstants.Logging.IS_DEBUG) {
            Log.i(AppConstants.Logging.TAG, message)
        }
    }

    fun w(message: String) {
        if (AppConstants.Logging.IS_DEBUG) {
            Log.w(AppConstants.Logging.TAG, message)
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
