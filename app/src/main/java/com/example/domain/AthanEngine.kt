package com.example.domain

import com.example.data.local.entity.PrayerSettingsEntity
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

data class PrayerTime(
    val name: String,
    val timeFormatted: String,
    val rawHour: Double,
    val isNext: Boolean = false,
    val minutesUntil: Int = 0
)

data class DailyPrayerTimes(
    val fajr: PrayerTime,
    val sunrise: PrayerTime,
    val dhuhr: PrayerTime,
    val asr: PrayerTime,
    val maghrib: PrayerTime,
    val isha: PrayerTime,
    val nextPrayerName: String,
    val countdownFormatted: String
)

object AthanEngine {

    fun calculatePrayerTimes(
        settings: PrayerSettingsEntity,
        date: Date = Date()
    ): DailyPrayerTimes {
        val cal = Calendar.getInstance()
        cal.time = date

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val timeZoneOffset = TimeZone.getDefault().getOffset(date.time) / 3600000.0

        // Julian Date Calculation
        val julianDate = getJulianDate(year, month, day)
        val d = julianDate - 2451545.0

        // Sun parameters
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val L = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g))

        val e = 23.439 - 0.00000036 * d
        val RA = atan2(dcos(e) * dsin(L), dcos(L)) / Math.PI * 18.0
        val eqt = q / 15.0 - fixHour(RA)
        val decl = asin(dsin(e) * dsin(L)) / Math.PI * 180.0

        val lat = settings.latitude
        val lng = settings.longitude

        // Solar Noon (Dhuhr base time)
        val dhuhrBase = 12.0 + timeZoneOffset - lng / 15.0 - eqt

        // Sun Angles (MWL method: Fajr 18°, Isha 17°)
        val fajrAngle = 18.0
        val ishaAngle = 17.0

        // Times in hours
        val fajrHour = dhuhrBase - hourAngle(-fajrAngle, lat, decl) / 15.0 + (settings.fajrOffsetMinutes / 60.0)
        val sunriseHour = dhuhrBase - hourAngle(-0.833, lat, decl) / 15.0
        val dhuhrHour = dhuhrBase + (settings.dhuhrOffsetMinutes / 60.0)
        
        // Asr (Standard / Shafi: shadow length = 1)
        val asrAngle = -atan(1.0 + tan(Math.toRadians(abs(lat - decl)))) * 180.0 / Math.PI
        val asrHour = dhuhrBase + hourAngle(asrAngle, lat, decl) / 15.0 + (settings.asrOffsetMinutes / 60.0)

        val maghribHour = dhuhrBase + hourAngle(-0.833, lat, decl) / 15.0 + (settings.maghribOffsetMinutes / 60.0)
        val ishaHour = dhuhrBase + hourAngle(-ishaAngle, lat, decl) / 15.0 + (settings.ishaOffsetMinutes / 60.0)

        val currentHour = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0

        val list = listOf(
            "Fajr" to fajrHour,
            "Sunrise" to sunriseHour,
            "Dhuhr" to dhuhrHour,
            "Asr" to asrHour,
            "Maghrib" to maghribHour,
            "Isha" to ishaHour
        )

        var nextName = "Fajr"
        var minDiff = 24.0 * 60

        for ((name, hour) in list) {
            val diffMinutes = (hour - currentHour) * 60
            if (diffMinutes > 0 && diffMinutes < minDiff) {
                minDiff = diffMinutes
                nextName = name
            }
        }

        val hoursUntil = (minDiff / 60).toInt()
        val minsUntil = (minDiff % 60).toInt()
        val countdown = if (minDiff >= 24 * 60) "Tomorrow" else String.format(Locale.getDefault(), "In %dh %02dm", hoursUntil, minsUntil)

        return DailyPrayerTimes(
            fajr = PrayerTime("Fajr", formatTime(fajrHour), fajrHour, nextName == "Fajr"),
            sunrise = PrayerTime("Sunrise", formatTime(sunriseHour), sunriseHour, nextName == "Sunrise"),
            dhuhr = PrayerTime("Dhuhr", formatTime(dhuhrHour), dhuhrHour, nextName == "Dhuhr"),
            asr = PrayerTime("Asr", formatTime(asrHour), asrHour, nextName == "Asr"),
            maghrib = PrayerTime("Maghrib", formatTime(maghribHour), maghribHour, nextName == "Maghrib"),
            isha = PrayerTime("Isha", formatTime(ishaHour), ishaHour, nextName == "Isha"),
            nextPrayerName = nextName,
            countdownFormatted = countdown
        )
    }

    private fun hourAngle(angle: Double, lat: Double, decl: Double): Double {
        val top = dsin(angle) - dsin(lat) * dsin(decl)
        val bottom = dcos(lat) * dcos(decl)
        val cosHA = top / bottom
        val clamped = cosHA.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clamped))
    }

    private fun getJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun formatTime(hourDouble: Double): String {
        val normalized = fixHour(hourDouble)
        val hours = normalized.toInt()
        val minutes = ((normalized - hours) * 60).toInt()
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour % 24.0
        if (h < 0) h += 24.0
        return h
    }

    private fun dsin(d: Double) = sin(Math.toRadians(d))
    private fun dcos(d: Double) = cos(Math.toRadians(d))
}
