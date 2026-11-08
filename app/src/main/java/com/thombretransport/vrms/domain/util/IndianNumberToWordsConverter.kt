package com.thombretransport.vrms.domain.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Converts numerical currency figures into text formatted strictly according
 * to the Indian Numbering System (Crore, Lakh, Thousand, Hundred, Units)
 * with exact Paise handling.
 *
 * Example:
 * 125050.75 -> "Rupees One Lakh Twenty-Five Thousand Fifty and Seventy-Five Paise Only"
 * 500.00 -> "Rupees Five Hundred Only"
 * 0.00 -> "Zero Rupees Only"
 */
object IndianNumberToWordsConverter {

    private val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun convert(amount: Double): String {
        if (amount < 0.0) return "Negative Amounts Not Supported"
        if (amount == 0.0) return "Zero Rupees Only"

        val bd = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
        val rupees = bd.toBigInteger().toLong()
        val paise = bd.remainder(BigDecimal.ONE).multiply(BigDecimal(100)).toInt()

        val words = StringBuilder()
        if (rupees > 0) {
            words.append("Rupees ").append(convertNumber(rupees))
        }

        if (paise > 0) {
            if (words.isNotEmpty()) words.append(" and ")
            words.append(convertTwoDigits(paise)).append(" Paise")
        }

        words.append(" Only")
        return words.toString().trim()
    }

    private fun convertNumber(n: Long): String {
        if (n == 0L) return ""
        if (n < 20) return units[n.toInt()]
        if (n < 100) return tens[(n / 10).toInt()] + (if (n % 10 != 0L) " " + units[(n % 10).toInt()] else "")
        if (n < 1000) return units[(n / 100).toInt()] + " Hundred" + (if (n % 100 != 0L) " " + convertNumber(n % 100) else "")
        if (n < 100000) return convertNumber(n / 1000) + " Thousand" + (if (n % 1000 != 0L) " " + convertNumber(n % 1000) else "")
        if (n < 10000000) return convertNumber(n / 100000) + " Lakh" + (if (n % 100000 != 0L) " " + convertNumber(n % 100000) else "")
        return convertNumber(n / 10000000) + " Crore" + (if (n % 10000000 != 0L) " " + convertNumber(n % 10000000) else "")
    }

    private fun convertTwoDigits(n: Int): String {
        return if (n < 20) units[n] else tens[n / 10] + (if (n % 10 != 0) " " + units[n % 10] else "")
    }
}
