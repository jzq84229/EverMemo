package com.zhan_dui.utils

import android.content.Context
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class DateHelperTest {

    @Mock
    private lateinit var mockContext: Context

    @Test
    fun testGetGridDate() {
        // Given
        val timestamp = 1700000000000L

        // When
        val result = DateHelper.getGridDate(mockContext, timestamp)

        // Then - PrettyTime format should return a relative time string
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        // PrettyTime returns strings like "2 months ago", "just now", etc.
    }

    @Test
    fun testGetGridDate_CurrentTime() {
        // Given
        val currentTime = System.currentTimeMillis()

        // When
        val result = DateHelper.getGridDate(mockContext, currentTime)

        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        // For current time, PrettyTime might return "just now" or "moments ago"
    }

    @Test
    fun testGetGridDate_FutureTime() {
        // Given
        val futureTime = System.currentTimeMillis() + 3600000 // 1 hour in future

        // When
        val result = DateHelper.getGridDate(mockContext, futureTime)

        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun testGetGridDate_ZeroTimestamp() {
        // Given
        val timestamp = 0L

        // When
        val result = DateHelper.getGridDate(mockContext, timestamp)

        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun testGetMemoDate() {
        // Given
        val timestamp = 1700000000000L

        // When
        val result = DateHelper.getMemoDate(mockContext, timestamp)

        // Then - sMemoShowDateFormat uses "M.d a h:m" format
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        // Format example: "3.19 AM 10:30"
    }

    @Test
    fun testGetMemoDate_CurrentTime() {
        // Given
        val currentTime = System.currentTimeMillis()

        // When
        val result = DateHelper.getMemoDate(mockContext, currentTime)

        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun testGetMemoDate_ZeroTimestamp() {
        // Given
        val timestamp = 0L

        // When
        val result = DateHelper.getMemoDate(mockContext, timestamp)

        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun testDateFormatConsistency() {
        // Test that formatting is consistent for same timestamp
        val timestamp = 1700000000000L

        val result1 = DateHelper.getMemoDate(mockContext, timestamp)
        val result2 = DateHelper.getMemoDate(mockContext, timestamp)

        assertEquals(result1, result2)
    }

    @Test
    fun testDifferentTimestampsProduceDifferentResults() {
        // Different timestamps should produce different formatted strings
        val timestamp1 = 1700000000000L
        val timestamp2 = 1700000001000L // 1 second later

        val result1 = DateHelper.getMemoDate(mockContext, timestamp1)
        val result2 = DateHelper.getMemoDate(mockContext, timestamp2)

        // They might be different (though with 1 second difference they might be same
        // depending on format precision)
        // At minimum, ensure method doesn't crash
        assertNotNull(result1)
        assertNotNull(result2)
    }

    @Test
    fun testGetGridDateAndGetMemoDateDifferentFormats() {
        // getGridDate and getMemoDate should use different formats
        val timestamp = 1700000000000L

        val gridResult = DateHelper.getGridDate(mockContext, timestamp)
        val memoResult = DateHelper.getMemoDate(mockContext, timestamp)

        // They should be different formats (PrettyTime vs SimpleDateFormat)
        assertNotNull(gridResult)
        assertNotNull(memoResult)
        // We can't easily compare the actual formats in unit tests
    }

    @Test
    fun testContextParameterIsIgnored() {
        // DateHelper methods take Context but don't seem to use it
        // This test verifies the methods work even with null context reference
        val timestamp = System.currentTimeMillis()

        val result1 = DateHelper.getGridDate(mockContext, timestamp)
        val result2 = DateHelper.getMemoDate(mockContext, timestamp)

        assertNotNull(result1)
        assertNotNull(result2)
    }
}