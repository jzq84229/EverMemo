package com.zhan_dui.utils

import org.junit.Test
import java.security.MessageDigest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MD5Test {

    @Test
    fun testGetMD5() {
        // Given
        val input = "test string"
        val expected = "6f8db599de986fab7a21625b7916589c" // MD5 of "test string"

        // When
        val result = MD5.getMD5(input)

        // Then
        assertNotNull(result)
        assertEquals(expected, result)
    }

    @Test
    fun testGetMD5_EmptyString() {
        // Given
        val input = ""
        val expected = "d41d8cd98f00b204e9800998ecf8427e" // MD5 of empty string

        // When
        val result = MD5.getMD5(input)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun testGetMD5_NullInput() {
        // Given
        val input: String? = null

        // When
        val result = MD5.getMD5(input)

        // Then
        assertEquals("", result)
    }

    @Test
    fun testGetMD5_SpecialCharacters() {
        // Given
        val input = "test@123#特殊字符"
        // Calculate expected MD5
        val md5 = MessageDigest.getInstance("MD5")
        val digest = md5.digest(input.toByteArray())
        val expected = digest.joinToString("") { "%02x".format(it) }

        // When
        val result = MD5.getMD5(input)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun testGetMD5_LongString() {
        // Given
        val input = "a".repeat(1000)
        // Calculate expected MD5
        val md5 = MessageDigest.getInstance("MD5")
        val digest = md5.digest(input.toByteArray())
        val expected = digest.joinToString("") { "%02x".format(it) }

        // When
        val result = MD5.getMD5(input)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun testGetMD5Consistency() {
        // MD5 should produce same output for same input
        val input = "consistent input"
        val result1 = MD5.getMD5(input)
        val result2 = MD5.getMD5(input)
        val result3 = MD5.getMD5(input)

        assertEquals(result1, result2)
        assertEquals(result2, result3)
        assertEquals(result1, result3)
    }

    @Test
    fun testGetMD5LowerCase() {
        // MD5 should be in lowercase hex
        val input = "Test Case"
        val result = MD5.getMD5(input)

        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.isLowerCase() || it.isDigit() })
        assertEquals(32, result.length) // MD5 hash is 32 characters
    }
}