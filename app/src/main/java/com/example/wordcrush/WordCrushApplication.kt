package com.example.wordcrush

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WordCrushApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 全局初始化可以在这里进行
    }
}
