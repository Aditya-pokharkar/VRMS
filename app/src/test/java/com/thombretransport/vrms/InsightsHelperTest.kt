package com.thombretransport.vrms

import com.thombretransport.vrms.domain.util.InsightsHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InsightsHelperTest {

    @Test
    fun testFormatRupeesZero() {
        val result = InsightsHelper.formatRupees(0.0)
        assertEquals("₹ 0", result)
    }

    @Test
    fun testFormatRupeesStandardAmounts() {
        val thousands = InsightsHelper.formatRupees(25000.0)
        assertEquals("₹ 25,000", thousands)

        val lakhs = InsightsHelper.formatRupees(142500.0)
        assertEquals("₹ 1,42,500", lakhs)

        val withDecimals = InsightsHelper.formatRupees(142500.50, withDecimals = true)
        assertEquals("₹ 1,42,500.50", withDecimals)
    }

    @Test
    fun testCalculateDaysIdleRecentTrip() {
        // Today's date
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Calendar.getInstance().time)
        val daysIdle = InsightsHelper.calculateDaysIdle(
            lastTripDate = todayStr,
            lastTripCreatedAt = System.currentTimeMillis(),
            vehicleCreatedAt = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000)
        )
        assertEquals(0, daysIdle)
    }

    @Test
    fun testCalculateDaysIdleOldTrip() {
        // 10 days ago
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -10)
        val tenDaysAgoStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.time)
        val tenDaysAgoMillis = cal.timeInMillis

        val daysIdle = InsightsHelper.calculateDaysIdle(
            lastTripDate = tenDaysAgoStr,
            lastTripCreatedAt = tenDaysAgoMillis,
            vehicleCreatedAt = tenDaysAgoMillis - (5L * 24 * 60 * 60 * 1000)
        )
        assertTrue(daysIdle in 9..11)
    }

    @Test
    fun testCalculateDaysIdleNoTripsUsesVehicleCreation() {
        // Created 14 days ago, never had a trip
        val fourteenDaysAgo = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000)
        val daysIdle = InsightsHelper.calculateDaysIdle(
            lastTripDate = null,
            lastTripCreatedAt = null,
            vehicleCreatedAt = fourteenDaysAgo
        )
        assertTrue(daysIdle in 13..15)
    }

    @Test
    fun testMonthTimestamps() {
        val startOfThisMonth = InsightsHelper.getStartOfMonthMillis()
        val startOfLastMonth = InsightsHelper.getStartOfLastMonthMillis()
        assertTrue(startOfThisMonth > startOfLastMonth)
    }
}
