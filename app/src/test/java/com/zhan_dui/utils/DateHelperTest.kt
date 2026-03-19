package com.zhan_dui.utils

import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateHelperTest {

    @Test
    fun testGetCurrentTime() {
        // Given
        val before = System.currentTimeMillis()

        // When
        val currentTime = DateHelper.getCurrentTime()
        val after = System.currentTimeMillis()

        // Then
        assertTrue(currentTime >= before)
        assertTrue(currentTime <= after)
    }

    @Test
    fun testGetTimeString() {
        // Given
        val timestamp = 1700000000000L // A specific timestamp
        val expectedFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val expected = expectedFormat.format(Date(timestamp))

        // When
        val result = DateHelper.getTimeString(timestamp)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun testGetTimeString_CurrentTime() {
        // When getting current time as string
        val result = DateHelper.getTimeString(System.currentTimeMillis())

        // Should match format
        val regex = """\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""".toRegex()
        assertTrue(regex.matches(result))
    }

    @Test
    fun testGetTimeString_ZeroTimestamp() {
        // Given
        val timestamp = 0L
        val expectedFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val expected = expectedFormat.format(Date(timestamp))

        // When
        val result = DateHelper.getTimeString(timestamp)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun testGetTimeString_NegativeTimestamp() {
        // Given
        val timestamp = -1000L
        val expectedFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val expected = expectedFormat.format(Date(timestamp))

        // When
        val result = DateHelper.getTimeString(timestamp)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun testGetCurrentTimeString() {
        // When
        val result = DateHelper.getCurrentTimeString()

        // Then
        val regex = """\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""".toRegex()
        assertTrue(regex.matches(result))
    }

    @Test
    fun testTimeFormatConsistency() {
        // Test that formatting is consistent
        val timestamp1 = 1700000000000L
        val timestamp2 = 1700000000000L // Same timestamp

        val result1 = DateHelper.getTimeString(timestamp1)
        val result2 = DateHelper.getTimeString(timestamp2)

        assertEquals(result1, result2)
    }

    @Test
    fun testGetCurrentTimeIncreases() {
        // Current time should increase between calls
        val time1 = DateHelper.getCurrentTime()
        Thread.sleep(10) // Small delay
        val time2 = DateHelper.getCurrentTime()

        assertTrue(time2 > time1)
    }

    @Test
    fun testDateFormatLocale() {
        // Format should use default locale
        val timestamp = 1700000000000L
        val result = DateHelper.getTimeString(timestamp)

        // Check format pattern (should be yyyy-MM-dd HH:mm:ss)
        assertTrue(result.contains("-")) // Date separator
        assertTrue(result.contains(":")) // Time separator
        assertTrue(result.contains(" ")) // Space between date and time
    }

    @Test
    fun testGetTimeStringWithDifferentTimezones() {
        // Note: DateHelper uses default timezone
        val timestamp = 1700000000000L
        val result = DateHelper.getTimeString(timestamp)

        // Result should not be empty
        assertTrue(result.isNotEmpty())
        assertTrue(result.length >= 19) // yyyy-MM-dd HH:mm:ss is 19 chars
    }
}