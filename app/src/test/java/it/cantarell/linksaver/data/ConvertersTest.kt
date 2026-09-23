package it.cantarell.linksaver.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun instant_roundTripsThroughEpochMillis() {
        val instant = Instant.parse("2026-09-23T17:30:21.123Z")
        val millis = converters.instantToEpochMillis(instant)
        assertEquals(instant, converters.epochMillisToInstant(millis))
    }

    @Test
    fun tags_roundTrip() {
        val tags = listOf("news", "tech, stuff", "a b")
        assertEquals(tags, converters.stringToTags(converters.tagsToString(tags)))
    }

    @Test
    fun emptyTags_roundTrip() {
        assertEquals("", converters.tagsToString(emptyList()))
        assertEquals(emptyList<String>(), converters.stringToTags(""))
    }
}
