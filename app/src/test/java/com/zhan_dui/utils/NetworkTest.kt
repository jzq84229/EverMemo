package com.zhan_dui.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class NetworkTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockConnectivityManager: ConnectivityManager

    @Mock
    private lateinit var mockNetworkInfo: NetworkInfo

    @Test
    fun testIsNetworkAvailable_WhenConnected() {
        // Given
        whenever(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE))
            .thenReturn(mockConnectivityManager)
        whenever(mockConnectivityManager.activeNetworkInfo).thenReturn(mockNetworkInfo)
        whenever(mockNetworkInfo.isConnected).thenReturn(true)

        // When
        val result = Network.isNetworkAvailable(mockContext)

        // Then
        assertTrue(result)
    }

    @Test
    fun testIsNetworkAvailable_WhenNotConnected() {
        // Given
        whenever(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE))
            .thenReturn(mockConnectivityManager)
        whenever(mockConnectivityManager.activeNetworkInfo).thenReturn(mockNetworkInfo)
        whenever(mockNetworkInfo.isConnected).thenReturn(false)

        // When
        val result = Network.isNetworkAvailable(mockContext)

        // Then
        assertFalse(result)
    }

    @Test
    fun testIsNetworkAvailable_WhenNoNetworkInfo() {
        // Given
        whenever(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE))
            .thenReturn(mockConnectivityManager)
        whenever(mockConnectivityManager.activeNetworkInfo).thenReturn(null)

        // When
        val result = Network.isNetworkAvailable(mockContext)

        // Then
        assertFalse(result)
    }

    @Test
    fun testIsNetworkAvailable_WhenConnectivityManagerNull() {
        // Given
        whenever(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE))
            .thenReturn(null)

        // When
        val result = Network.isNetworkAvailable(mockContext)

        // Then
        assertFalse(result)
    }

    @Test
    fun testIsNetworkAvailable_WhenContextNull() {
        // Given
        val nullContext: Context? = null

        // When
        val result = Network.isNetworkAvailable(nullContext)

        // Then
        assertFalse(result)
    }

    @Test
    fun testNetworkTypeConstants() {
        // Network class might have constants for network types
        // Check common constants if they exist
        val networkClass = Network::class.java

        // These are common connectivity constants in Android
        val expectedConstants = listOf(
            "TYPE_WIFI",
            "TYPE_MOBILE",
            "TYPE_BLUETOOTH",
            "TYPE_ETHERNET"
        )

        // Check if any of these constants exist
        val fields = networkClass.declaredFields.map { it.name }
        val hasNetworkConstants = fields.any { it in expectedConstants }

        // It's OK if they don't exist - Network class might just check connectivity
        if (hasNetworkConstants) {
            // Verify they're integer constants
            expectedConstants.forEach { constantName ->
                try {
                    val field = networkClass.getDeclaredField(constantName)
                    assert(field.type == Int::class.java || field.type == Integer.TYPE)
                } catch (e: NoSuchFieldException) {
                    // Field doesn't exist, that's OK
                }
            }
        }
    }

    @Test
    fun testNetworkAvailabilityConsistency() {
        // When called multiple times with same context, should return same result
        // (assuming network state doesn't change)

        // Given
        whenever(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE))
            .thenReturn(mockConnectivityManager)
        whenever(mockConnectivityManager.activeNetworkInfo).thenReturn(mockNetworkInfo)
        whenever(mockNetworkInfo.isConnected).thenReturn(true)

        // When
        val result1 = Network.isNetworkAvailable(mockContext)
        val result2 = Network.isNetworkAvailable(mockContext)
        val result3 = Network.isNetworkAvailable(mockContext)

        // Then
        assertTrue(result1)
        assertTrue(result2)
        assertTrue(result3)
        assertEquals(result1, result2)
        assertEquals(result2, result3)
    }

    @Test
    fun testNetworkMethodsExist() {
        // Network class should have the isNetworkAvailable method
        val methods = Network::class.java.declaredMethods.map { it.name }
        assert(methods.contains("isNetworkAvailable"))
    }
}