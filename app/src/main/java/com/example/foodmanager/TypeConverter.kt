package com.example.foodmanager

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromStepsList(steps: List<String>?): String? {
        return gson.toJson(steps)
    }

    @TypeConverter
    fun toStepsList(stepsString: String?): List<String>? {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stepsString, type)
    }
}
