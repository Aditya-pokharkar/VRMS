package com.thombretransport.vrms.domain.util

/**
 * Validates and normalizes Indian RTO registration numbers.
 * Supports standard RTO format (e.g., MH14XX1234) and Bharat Series (e.g., 22BH1234AA).
 */
object RegistrationNumberValidator {

    // Standard RTO: 2-letter state code + 2-digit district + 1-3 letters series + 4 digits
    private val STANDARD_RTO_REGEX = Regex("^[A-Z]{2}[0-9]{2}[A-Z]{1,3}[0-9]{4}$")

    // Bharat Series: 2-digit year + BH + 4 digits + 1-2 letters
    private val BHARAT_SERIES_REGEX = Regex("^[0-9]{2}BH[0-9]{4}[A-Z]{1,2}$")

    /**
     * Normalizes input by stripping whitespace, hyphens, and converting to uppercase.
     */
    fun normalize(input: String): String {
        return input.uppercase().replace(Regex("[\\s-]+"), "")
    }

    /**
     * Validates whether the normalized string conforms to Indian registration standards.
     */
    fun isValid(input: String): Boolean {
        val normalized = normalize(input)
        return STANDARD_RTO_REGEX.matches(normalized) || BHARAT_SERIES_REGEX.matches(normalized)
    }

    /**
     * Formats normalized string for user-friendly display (e.g., MH 14 XX 1234).
     */
    fun formatForDisplay(normalized: String): String {
        val clean = normalize(normalized)
        if (BHARAT_SERIES_REGEX.matches(clean)) {
            // 22BH1234AA -> 22 BH 1234 AA
            return "${clean.substring(0, 2)} ${clean.substring(2, 4)} ${clean.substring(4, 8)} ${clean.substring(8)}"
        }
        if (STANDARD_RTO_REGEX.matches(clean)) {
            // MH14XX1234 -> MH 14 XX 1234
            val state = clean.substring(0, 2)
            val rto = clean.substring(2, 4)
            val digits = clean.takeLast(4)
            val series = clean.substring(4, clean.length - 4)
            return "$state $rto $series $digits".trim()
        }
        return clean
    }
}
