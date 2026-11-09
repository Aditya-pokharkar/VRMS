package com.thombretransport.vrms.domain.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object InsightsHelper {

    fun getStartOfMonthMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getStartOfLastMonthMillis(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getSevenDaysAgoMillis(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -7)
        return cal.timeInMillis
    }

    fun getCurrentMonthName(): String {
        return SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date())
    }

    fun getLastMonthName(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        return SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(cal.time)
    }

    fun formatRupees(amount: Double, withDecimals: Boolean = false): String {
        val isNegative = amount < 0
        val absAmount = kotlin.math.abs(amount)
        val rounded = if (withDecimals) {
            String.format(Locale.ENGLISH, "%.2f", absAmount)
        } else {
            String.format(Locale.ENGLISH, "%.0f", kotlin.math.round(absAmount))
        }

        val parts = rounded.split(".")
        val intPart = parts[0]
        val decPart = if (parts.size > 1 && withDecimals) "." + parts[1] else ""

        val formattedNumber = if (intPart.length <= 3) {
            "$intPart$decPart"
        } else {
            val last3 = intPart.substring(intPart.length - 3)
            val remaining = intPart.substring(0, intPart.length - 3)

            val sb = StringBuilder()
            var count = 0
            for (i in remaining.length - 1 downTo 0) {
                sb.append(remaining[i])
                count++
                if (count == 2 && i != 0) {
                    sb.append(",")
                    count = 0
                }
            }
            val groupedRemaining = sb.reverse().toString()
            "$groupedRemaining,$last3$decPart"
        }

        return if (isNegative) "-₹ $formattedNumber" else "₹ $formattedNumber"
    }

    /**
     * Calculates how many days a vehicle has been without any recorded trip.
     * Uses the latest of:
     * - parsed trip_date (YYYY-MM-DD)
     * - trip created_at
     * - or vehicle created_at if no trips were ever recorded.
     */
    fun calculateDaysIdle(lastTripDate: String?, lastTripCreatedAt: Long?, vehicleCreatedAt: Long): Int {
        val now = System.currentTimeMillis()
        var latestActivityTimestamp = vehicleCreatedAt

        if (!lastTripDate.isNullOrBlank()) {
            try {
                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(lastTripDate)
                if (parsed != null && parsed.time > latestActivityTimestamp) {
                    latestActivityTimestamp = parsed.time
                }
            } catch (_: Exception) { }
        }

        if (lastTripCreatedAt != null && lastTripCreatedAt > latestActivityTimestamp) {
            latestActivityTimestamp = lastTripCreatedAt
        }

        val diffMillis = now - latestActivityTimestamp
        if (diffMillis <= 0) return 0
        return (diffMillis / (1000L * 60 * 60 * 24)).toInt()
    }
}
