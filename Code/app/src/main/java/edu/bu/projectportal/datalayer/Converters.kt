package edu.bu.projectportal.datalayer

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromStringSet(value: Set<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringSet(value: String): Set<String> {
        val setType = object : TypeToken<Set<String>>() {}.type
        return Gson().fromJson(value, setType)
    }

}
