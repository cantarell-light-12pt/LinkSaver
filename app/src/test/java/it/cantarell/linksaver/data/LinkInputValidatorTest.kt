package it.cantarell.linksaver.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkInputValidatorTest {

    private fun valid(url: String) = UrlValidation.Valid(url)
    private fun invalid(error: UrlError) = UrlValidation.Invalid(error)

    @Test
    fun validateUrl_blankInput_isEmptyError() {
        assertEquals(invalid(UrlError.EMPTY), LinkInputValidator.validateUrl(""))
        assertEquals(invalid(UrlError.EMPTY), LinkInputValidator.validateUrl("   "))
    }

    @Test
    fun validateUrl_keepsHttpAndHttpsUrls() {
        assertEquals(valid("https://example.com/a?b=c#d"), LinkInputValidator.validateUrl("https://example.com/a?b=c#d"))
        assertEquals(valid("http://example.com"), LinkInputValidator.validateUrl("  http://example.com  "))
        assertEquals(valid("HTTPS://Example.com"), LinkInputValidator.validateUrl("HTTPS://Example.com"))
    }

    @Test
    fun validateUrl_addsHttpsWhenSchemeMissing() {
        assertEquals(valid("https://example.com/path"), LinkInputValidator.validateUrl("example.com/path"))
    }

    @Test
    fun validateUrl_acceptsLocalhostAndIpAddresses() {
        assertEquals(valid("http://localhost:8080"), LinkInputValidator.validateUrl("http://localhost:8080"))
        assertEquals(valid("https://192.168.1.1"), LinkInputValidator.validateUrl("192.168.1.1"))
        assertEquals(valid("http://[::1]/x"), LinkInputValidator.validateUrl("http://[::1]/x"))
    }

    @Test
    fun validateUrl_rejectsOtherSchemes() {
        assertEquals(invalid(UrlError.INVALID), LinkInputValidator.validateUrl("ftp://example.com"))
        assertEquals(invalid(UrlError.INVALID), LinkInputValidator.validateUrl("javascript://example.com"))
    }

    @Test
    fun validateUrl_rejectsHostsThatAreNotDomains() {
        assertEquals(invalid(UrlError.INVALID), LinkInputValidator.validateUrl("hello"))
        assertEquals(invalid(UrlError.INVALID), LinkInputValidator.validateUrl("example."))
        assertEquals(invalid(UrlError.INVALID), LinkInputValidator.validateUrl("https://"))
    }

    @Test
    fun validateUrl_rejectsMalformedUrls() {
        assertEquals(invalid(UrlError.INVALID), LinkInputValidator.validateUrl("exa mple.com"))
        assertEquals(invalid(UrlError.INVALID), LinkInputValidator.validateUrl("https://exa<mple.com"))
    }

    @Test
    fun isSingleEmoji_acceptsSingleEmoji() {
        assertTrue(LinkInputValidator.isSingleEmoji("🔗"))
        assertTrue(LinkInputValidator.isSingleEmoji("❤️"))
        assertTrue(LinkInputValidator.isSingleEmoji("👍🏽"))
        assertTrue(LinkInputValidator.isSingleEmoji("👨‍👩‍👧"))
        assertTrue(LinkInputValidator.isSingleEmoji("🇮🇹"))
    }

    @Test
    fun isSingleEmoji_rejectsTextAndMultipleEmoji() {
        assertFalse(LinkInputValidator.isSingleEmoji(""))
        assertFalse(LinkInputValidator.isSingleEmoji("a"))
        assertFalse(LinkInputValidator.isSingleEmoji("1"))
        assertFalse(LinkInputValidator.isSingleEmoji("🔗🔗"))
        assertFalse(LinkInputValidator.isSingleEmoji("🔗a"))
    }

    @Test
    fun normalizeTag_trimsCollapsesAndStripsControlCharacters() {
        assertEquals("a b", LinkInputValidator.normalizeTag("  a \t  b "))
        assertEquals("a b", LinkInputValidator.normalizeTag("a\u001Fb"))
        assertNull(LinkInputValidator.normalizeTag("   "))
    }

    @Test
    fun mergeTags_splitsOnCommasAndSkipsDuplicates() {
        assertEquals(
            listOf("News", "tech", "kotlin"),
            LinkInputValidator.mergeTags(listOf("News"), "news, tech,, kotlin , TECH"),
        )
    }

    @Test
    fun mergeTags_blankInputKeepsExisting() {
        assertEquals(listOf("a"), LinkInputValidator.mergeTags(listOf("a"), "  "))
    }
}
