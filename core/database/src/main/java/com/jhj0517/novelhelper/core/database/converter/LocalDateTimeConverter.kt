package com.jhj0517.novelhelper.core.database.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Type converter for LocalDateTime to/from Long for Room database.
 */
class LocalDateTimeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }
} 