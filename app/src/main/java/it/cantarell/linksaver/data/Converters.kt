package it.cantarell.linksaver.data

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Room type converters. Tags are stored as a single column joined by [TAG_SEPARATOR], a control
 * character that tag normalization strips from user input, so it can never appear inside a tag.
 */
class Converters {
    @TypeConverter
    fun instantToEpochMillis(instant: Instant): Long = instant.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(epochMillis: Long): Instant = Instant.ofEpochMilli(epochMillis)

    @TypeConverter
    fun tagsToString(tags: List<String>): String = tags.joinToString(TAG_SEPARATOR)

    @TypeConverter
    fun stringToTags(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(TAG_SEPARATOR)

    companion object {
        const val TAG_SEPARATOR = "\u001F"
    }
}
