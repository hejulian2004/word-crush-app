package com.example.wordcrush.Database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    private val listType = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun fromString(value: String?): List<String>? =
        value?.let { gson.fromJson(it, listType) }

    @TypeConverter
    fun fromList(list: List<String>?): String = gson.toJson(list)
}
