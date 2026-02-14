package dev.harrisonsoftware.stitchCounter.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) {
            return emptyList()
        }
        return Json.decodeFromString(value)
    }
}
