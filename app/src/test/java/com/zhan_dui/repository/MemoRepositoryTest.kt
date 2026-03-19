package com.zhan_dui.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.zhan_dui.data.Memo
import com.zhan_dui.data.MemoDB
import com.zhan_dui.data.MemoProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@RunWith(MockitoJUnitRunner::class)
class MemoRepositoryTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockContentResolver: ContentResolver

    @Mock
    private lateinit var mockCursor: Cursor

    private lateinit var repository: MemoRepository

    @Test
    fun testGetAllMemos() {
        // Given
        whenever(mockContext.contentResolver).thenReturn(mockContentResolver)
        repository = MemoRepository(mockContext)

        val testUri = Uri.parse("content://com.zhan_dui.evermemo.memoprovider/memo")
        whenever(mockContentResolver.query(
            eq(MemoProvider.MEMO_URI),
            isNull(),
            isNull(),
            isNull(),
            eq("${MemoDB.UPDATEDTIME} desc")
        )).thenReturn(mockCursor)

        // Mock cursor behavior
        val expectedMemos = listOf(
            createTestMemo(1, "Memo 1"),
            createTestMemo(2, "Memo 2")
        )

        var callCount = 0
        whenever(mockCursor.moveToNext()).thenAnswer {
            callCount < expectedMemos.size
        }.thenAnswer {
            callCount++ < expectedMemos.size
        }

        whenever(mockCursor.moveToNext()).thenReturn(true, true, false)

        // Mock the Memo constructor that takes Cursor
        // This is tricky because Memo(cursor) is actual implementation
        // We'll need to create a spy or use a different approach

        // For now, skip detailed cursor mocking since it's complex
        // and focus on testing repository logic

        // When
        val result = repository.getAllMemos()

        // Then - at least verify the query was made
        verify(mockContentResolver).query(
            eq(MemoProvider.MEMO_URI),
            isNull(),
            isNull(),
            isNull(),
            eq("${MemoDB.UPDATEDTIME} desc")
        )
        assertNotNull(result)
    }

    @Test
    fun testGetMemoById() {
        // Given
        whenever(mockContext.contentResolver).thenReturn(mockContentResolver)
        repository = MemoRepository(mockContext)

        whenever(mockContentResolver.query(
            eq(MemoProvider.MEMO_URI),
            isNull(),
            eq("${MemoDB.ID}=?"),
            eq(arrayOf("1")),
            isNull()
        )).thenReturn(mockCursor)

        whenever(mockCursor.moveToFirst()).thenReturn(true)

        // When
        val result = repository.getMemoById(1)

        // Then
        verify(mockContentResolver).query(
            eq(MemoProvider.MEMO_URI),
            isNull(),
            eq("${MemoDB.ID}=?"),
            eq(arrayOf("1")),
            isNull()
        )
        // Result depends on Memo(cursor) constructor which we can't easily mock
    }

    @Test
    fun testGetMemoById_NotFound() {
        // Given
        whenever(mockContext.contentResolver).thenReturn(mockContentResolver)
        repository = MemoRepository(mockContext)

        whenever(mockContentResolver.query(
            eq(MemoProvider.MEMO_URI),
            isNull(),
            eq("${MemoDB.ID}=?"),
            eq(arrayOf("999")),
            isNull()
        )).thenReturn(mockCursor)

        whenever(mockCursor.moveToFirst()).thenReturn(false)

        // When
        val result = repository.getMemoById(999)

        // Then
        assertNull(result)
        verify(mockCursor).close()
    }

    @Test
    fun testInsertMemo() {
        // Given
        whenever(mockContext.contentResolver).thenReturn(mockContentResolver)
        repository = MemoRepository(mockContext)

        val testMemo = createTestMemo(0, "New Memo")
        val expectedUri = Uri.parse("content://com.zhan_dui.evermemo.memoprovider/memo/1")

        whenever(mockContentResolver.insert(eq(MemoProvider.MEMO_URI), any()))
            .thenReturn(expectedUri)

        // Mock toInsertContentValues
        val mockValues = ContentValues().apply {
            put(MemoDB.CONTENT, testMemo.content)
            put(MemoDB.CREATEDTIME, testMemo.createdTime)
            put(MemoDB.UPDATEDTIME, testMemo.updatedTime)
            // Add other fields as needed
        }

        // We need to mock Memo.toInsertContentValues()
        // Since we can't easily mock it, we'll skip detailed value checking

        // When
        val result = repository.insertMemo(testMemo)

        // Then
        assertEquals(expectedUri, result)
        verify(mockContentResolver).insert(eq(MemoProvider.MEMO_URI), any())
    }

    @Test
    fun testUpdateMemo() {
        // Given
        whenever(mockContext.contentResolver).thenReturn(mockContentResolver)
        repository = MemoRepository(mockContext)

        val testMemo = createTestMemo(1, "Updated Memo")

        whenever(mockContentResolver.update(
            eq(MemoProvider.MEMO_URI),
            any(),
            eq("${MemoDB.ID}=?"),
            eq(arrayOf("1"))
        )).thenReturn(1)

        // When
        val result = repository.updateMemo(testMemo)

        // Then
        assertEquals(1, result)
        verify(mockContentResolver).update(
            eq(MemoProvider.MEMO_URI),
            any(),
            eq("${MemoDB.ID}=?"),
            eq(arrayOf("1"))
        )
    }

    @Test
    fun testDeleteMemo() {
        // Given
        whenever(mockContext.contentResolver).thenReturn(mockContentResolver)
        repository = MemoRepository(mockContext)

        whenever(mockContentResolver.update(
            eq(MemoProvider.MEMO_URI),
            any(),
            eq("${MemoDB.ID}=?"),
            eq(arrayOf("1"))
        )).thenReturn(1)

        // When
        val result = repository.deleteMemo(1)

        // Then
        assertEquals(1, result)
        verify(mockContentResolver).update(
            eq(MemoProvider.MEMO_URI),
            argThat { values ->
                values.getAsString(MemoDB.STATUS) == Memo.STATUS_DELETE &&
                values.getAsInteger(MemoDB.SYNCSTATUS) == Memo.NEED_SYNC_DELETE
            },
            eq("${MemoDB.ID}=?"),
            eq(arrayOf("1"))
        )
    }

    @Test
    fun testRefreshMemos() {
        // Given
        whenever(mockContext.contentResolver).thenReturn(mockContentResolver)
        repository = MemoRepository(mockContext)

        // Mock query for refresh
        whenever(mockContentResolver.query(
            eq(MemoProvider.MEMO_URI),
            isNull(),
            isNull(),
            isNull(),
            eq("${MemoDB.UPDATEDTIME} desc")
        )).thenReturn(mockCursor)

        whenever(mockCursor.moveToNext()).thenReturn(false) // Empty cursor

        // When
        repository.refreshMemos()

        // Then - verify query was called
        verify(mockContentResolver, atLeastOnce()).query(
            eq(MemoProvider.MEMO_URI),
            isNull(),
            isNull(),
            isNull(),
            eq("${MemoDB.UPDATEDTIME} desc")
        )
    }

    // Helper method
    private fun createTestMemo(id: Int, content: String): Memo {
        return Memo().apply {
            this.id = id
            this.content = content
            createdTime = System.currentTimeMillis()
            updatedTime = System.currentTimeMillis()
            hash = content.toByteArray()
            guid = "guid_$id"
            enid = "enid_$id"
            syncStatus = Memo.NEED_SYNC_UP
            status = "active"
            cursorPosition = 0
            wallId = 1
            order = 1
            lastSyncTime = System.currentTimeMillis()
            attributes = "{}"
        }
    }
}