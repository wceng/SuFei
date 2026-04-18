package dev.wceng.sufei.data.local.room

import androidx.room.TypeConverter
import dev.wceng.sufei.data.model.PoetDescription
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromPoetDescriptionList(value: List<PoetDescription>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toPoetDescriptionList(value: String): List<PoetDescription> {
        return Json.decodeFromString(value)
    }
}
