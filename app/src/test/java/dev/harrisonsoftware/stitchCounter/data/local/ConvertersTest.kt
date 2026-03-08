package dev.harrisonsoftware.stitchCounter.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `round-trip preserves a list of strings`() {
        val original = listOf("path/img1.jpg", "path/img2.png", "another/photo.webp")
        val json = converters.fromStringList(original)
        val restored = converters.toStringList(json)
        assertEquals(original, restored)
    }

    @Test
    fun `round-trip preserves empty list`() {
        val original = emptyList<String>()
        val json = converters.fromStringList(original)
        val restored = converters.toStringList(json)
        assertEquals(original, restored)
    }

    @Test
    fun `toStringList returns empty list for blank input`() {
        assertEquals(emptyList<String>(), converters.toStringList(""))
        assertEquals(emptyList<String>(), converters.toStringList("   "))
    }

    @Test
    fun `round-trip preserves single element list`() {
        val original = listOf("only_one.jpg")
        val json = converters.fromStringList(original)
        val restored = converters.toStringList(json)
        assertEquals(original, restored)
    }

    @Test
    fun `round-trip preserves strings with special characters`() {
        val original = listOf("path/with spaces/img.jpg", "unicode_☺.png", "quotes\"here.jpg")
        val json = converters.fromStringList(original)
        val restored = converters.toStringList(json)
        assertEquals(original, restored)
    }
}
