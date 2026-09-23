package it.cantarell.linksaver.data

import java.net.URI
import java.net.URISyntaxException
import java.text.BreakIterator

enum class UrlError { EMPTY, INVALID }

sealed interface UrlValidation {
    data class Valid(val url: String) : UrlValidation
    data class Invalid(val error: UrlError) : UrlValidation
}

/** Pure validation and normalization of the user input for a new [Link]. */
object LinkInputValidator {
    private val ALLOWED_SCHEMES = setOf("http", "https")
    private const val DEFAULT_SCHEME = "https://"
    private val WHITESPACE = Regex("\\s+")
    private val CONTROL_CHARS = Regex("\\p{Cntrl}")

    /**
     * Validates a user-typed URL. Input without a scheme gets `https://`; only http(s) URLs
     * with a host that looks like a domain (contains a dot), `localhost` or an IPv6 literal pass.
     */
    fun validateUrl(raw: String): UrlValidation {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return UrlValidation.Invalid(UrlError.EMPTY)
        val candidate = if ("://" in trimmed) trimmed else DEFAULT_SCHEME + trimmed
        val uri = try {
            URI(candidate)
        } catch (_: URISyntaxException) {
            return UrlValidation.Invalid(UrlError.INVALID)
        }
        val scheme = uri.scheme?.lowercase()
        val host = uri.host
        val validHost = host != null &&
            ("." in host || host.equals("localhost", ignoreCase = true) || host.startsWith("["))
        return if (scheme in ALLOWED_SCHEMES && validHost && !host.endsWith(".")) {
            UrlValidation.Valid(candidate)
        } else {
            UrlValidation.Invalid(UrlError.INVALID)
        }
    }

    /** True when [value] is exactly one grapheme cluster that is an emoji. */
    fun isSingleEmoji(value: String): Boolean {
        if (value.isEmpty()) return false
        val graphemes = BreakIterator.getCharacterInstance()
        graphemes.setText(value)
        graphemes.first()
        if (graphemes.next() != value.length) return false
        val first = value.codePointAt(0)
        return Character.getType(first) == Character.OTHER_SYMBOL.toInt() || first in REGIONAL_INDICATORS
    }

    /** Trims, collapses whitespace and strips control characters; returns null for a blank tag. */
    fun normalizeTag(raw: String): String? =
        raw.replace(CONTROL_CHARS, " ").trim().replace(WHITESPACE, " ").ifEmpty { null }

    /**
     * Splits comma-separated [input] into normalized tags and appends the ones not already in
     * [existing], comparing case-insensitively.
     */
    fun mergeTags(existing: List<String>, input: String): List<String> {
        val result = existing.toMutableList()
        input.split(',').mapNotNull(::normalizeTag).forEach { tag ->
            if (result.none { it.equals(tag, ignoreCase = true) }) result += tag
        }
        return result
    }

    private val REGIONAL_INDICATORS = 0x1F1E6..0x1F1FF
}
