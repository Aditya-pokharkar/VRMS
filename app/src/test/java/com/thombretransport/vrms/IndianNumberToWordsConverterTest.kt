package com.thombretransport.vrms

import com.thombretransport.vrms.domain.util.IndianNumberToWordsConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class IndianNumberToWordsConverterTest {

    @Test
    fun testZeroAmount() {
        assertEquals("Zero Rupees Only", IndianNumberToWordsConverter.convert(0.0))
    }

    @Test
    fun testWholeRupees() {
        assertEquals("Rupees Five Hundred Only", IndianNumberToWordsConverter.convert(500.0))
        assertEquals("Rupees One Thousand Two Hundred Only", IndianNumberToWordsConverter.convert(1200.0))
    }

    @Test
    fun testLakhsAndThousands() {
        assertEquals(
            "Rupees One Lakh Twenty-Five Thousand Fifty Only",
            IndianNumberToWordsConverter.convert(125050.0)
        )
    }

    @Test
    fun testPaiseFractions() {
        assertEquals(
            "Rupees One Lakh Twenty-Five Thousand Fifty and Seventy-Five Paise Only",
            IndianNumberToWordsConverter.convert(125050.75)
        )
        assertEquals(
            "Rupees Five Hundred and Fifty Paise Only",
            IndianNumberToWordsConverter.convert(500.50)
        )
    }

    @Test
    fun testCrores() {
        assertEquals(
            "Rupees One Crore Twenty-Three Lakh Forty-Five Thousand Six Hundred Seventy-Eight Only",
            IndianNumberToWordsConverter.convert(12345678.0)
        )
    }
}
