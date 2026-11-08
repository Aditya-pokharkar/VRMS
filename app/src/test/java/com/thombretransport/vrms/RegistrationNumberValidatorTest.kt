package com.thombretransport.vrms

import com.thombretransport.vrms.domain.util.RegistrationNumberValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationNumberValidatorTest {

    @Test
    fun testStandardRtoFormats() {
        assertTrue(RegistrationNumberValidator.isValid("MH14XX1234"))
        assertTrue(RegistrationNumberValidator.isValid("MH 14 XX 1234"))
        assertTrue(RegistrationNumberValidator.isValid("mh-14-ab-5678"))
        assertTrue(RegistrationNumberValidator.isValid("DL01A1234"))
    }

    @Test
    fun testBharatSeriesFormats() {
        assertTrue(RegistrationNumberValidator.isValid("22BH1234AA"))
        assertTrue(RegistrationNumberValidator.isValid("22 BH 1234 AA"))
        assertTrue(RegistrationNumberValidator.isValid("21-bh-9999-z"))
    }

    @Test
    fun testInvalidFormats() {
        assertFalse(RegistrationNumberValidator.isValid("INVALID123"))
        assertFalse(RegistrationNumberValidator.isValid("12345"))
        assertFalse(RegistrationNumberValidator.isValid("M14XX1234"))
    }

    @Test
    fun testNormalizationAndFormatting() {
        assertEquals("MH14XX1234", RegistrationNumberValidator.normalize(" mh - 14 - xx 1234 "))
        assertEquals("MH 14 XX 1234", RegistrationNumberValidator.formatForDisplay("MH14XX1234"))
        assertEquals("22 BH 1234 AA", RegistrationNumberValidator.formatForDisplay("22BH1234AA"))
    }
}
