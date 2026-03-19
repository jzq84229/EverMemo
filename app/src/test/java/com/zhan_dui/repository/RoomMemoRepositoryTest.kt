package com.zhan_dui.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zhan_dui.data.AppDatabase
import com.zhan_dui.data.Memo
import com.zhan_dui.data.MemoDao
import com.zhan_dui.data.MemoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class RoomMemoRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockDatabase: AppDatabase

    @Mock
    private lateinit var mockMemoDao: MemoDao

    private lateinit var repository: RoomMemoRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(mockDatabase.memoDao()).thenReturn(mockMemoDao)
        // We need to use reflection or constructor injection for testing
        // For now, create a test version or use mock context
        repository = RoomMemoRepository(mockDatabase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGetAllMemos() = runTest {
        // Given
        val testEntities = listOf(
            createTestMemoEntity(1, "Memo 1"),
            createTestMemoEntity(2, "Memo 2")
        )
        val liveData = MutableLiveData<List<MemoEntity>>().apply {
            value = testEntities
        }
        whenever(mockMemoDao.getAllMemos()).thenReturn(liveData)

        // When
        val result = repository.getAllMemos()

        // Then
        assertNotNull(result)
        val memos = result.value
        assertNotNull(memos)
        assertEquals(2, memos!!.size)
        assertEquals("Memo 1", memos[0].content)
        assertEquals("Memo 2", memos[1].content)
    }

    @Test
    fun testGetMemoById() = runTest {
        // Given
        val testEntity = createTestMemoEntity(1, "Test Memo")
        whenever(mockMemoDao.getMemoById(1)).thenReturn(testEntity)

        // When
        val result = repository.getMemoById(1)

        // Then
        assertNotNull(result)
        assertEquals(1, result!!.id)
        assertEquals("Test Memo", result.content)
    }

    @Test
    fun testGetMemoById_NotFound() = runTest {
        // Given
        whenever(mockMemoDao.getMemoById(999)).thenReturn(null)

        // When
        val result = repository.getMemoById(999)

        // Then
        assertNull(result)
    }

    @Test
    fun testInsertMemo() = runTest {
        // Given
        val testMemo = createTestMemo(0, "New Memo")
        val testEntity = MemoEntity.fromMemo(testMemo)
        whenever(mockMemoDao.insert(any())).thenReturn(1L)

        var callbackCalled = false
        var insertedId: Long? = null
        var callbackError: Exception? = null

        val callback = object : RoomMemoRepository.InsertCallback {
            override fun onInserted(id: Long) {
                callbackCalled = true
                insertedId = id
            }

            override fun onError(e: Exception) {
                callbackError = e
            }
        }

        // When
        repository.insertMemo(testMemo, callback)

        // Wait for async operation
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(callbackCalled)
        assertEquals(1L, insertedId)
        assertNull(callbackError)
        verify(mockMemoDao).insert(any())
    }

    @Test
    fun testUpdateMemo() = runTest {
        // Given
        val testMemo = createTestMemo(1, "Updated Memo")
        val testEntity = MemoEntity.fromMemo(testMemo)
        whenever(mockMemoDao.update(any())).thenReturn(1)

        var callbackCalled = false
        var updateSuccess: Boolean? = null
        var callbackError: Exception? = null

        val callback = object : RoomMemoRepository.UpdateCallback {
            override fun onUpdated(success: Boolean) {
                callbackCalled = true
                updateSuccess = success
            }

            override fun onError(e: Exception) {
                callbackError = e
            }
        }

        // When
        repository.updateMemo(testMemo, callback)

        // Wait for async operation
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(callbackCalled)
        assertEquals(true, updateSuccess)
        assertNull(callbackError)
        verify(mockMemoDao).update(any())
    }

    @Test
    fun testDeleteMemo() = runTest {
        // Given
        val memoId = 1
        whenever(mockMemoDao.markAsDeleted(eq(memoId), eq(Memo.NEED_SYNC_DELETE))).thenReturn(1)

        var callbackCalled = false
        var deleteSuccess: Boolean? = null
        var callbackError: Exception? = null

        val callback = object : RoomMemoRepository.DeleteCallback {
            override fun onDeleted(success: Boolean) {
                callbackCalled = true
                deleteSuccess = success
            }

            override fun onError(e: Exception) {
                callbackError = e
            }
        }

        // When
        repository.deleteMemo(memoId, callback)

        // Wait for async operation
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(callbackCalled)
        assertEquals(true, deleteSuccess)
        assertNull(callbackError)
        verify(mockMemoDao).markAsDeleted(eq(memoId), eq(Memo.NEED_SYNC_DELETE))
    }

    @Test
    fun testGetMemosNeedingSync() = runTest {
        // Given
        val testEntities = listOf(
            createTestMemoEntity(1, "Memo 1", Memo.NEED_SYNC_UP),
            createTestMemoEntity(2, "Memo 2", Memo.NEED_SYNC_DELETE)
        )
        whenever(mockMemoDao.getMemosNeedingSync()).thenReturn(testEntities)

        // When
        val result = repository.getMemosNeedingSync()

        // Then
        assertEquals(2, result.size)
        assertEquals("Memo 1", result[0].content)
        assertEquals("Memo 2", result[1].content)
        assertTrue(result.all { it.syncStatus != Memo.SYNC_SUCCESS })
    }

    @Test
    fun testInsertMemoSync() = runTest {
        // Given
        val testMemo = createTestMemo(0, "Sync Insert Memo")
        whenever(mockMemoDao.insert(any())).thenReturn(42L)

        // When
        val result = repository.insertMemoSync(testMemo)

        // Then
        assertEquals(42L, result)
        verify(mockMemoDao).insert(any())
    }

    @Test
    fun testInsertMemoSync_Error() = runTest {
        // Given
        val testMemo = createTestMemo(0, "Error Memo")
        whenever(mockMemoDao.insert(any())).thenThrow(RuntimeException("Database error"))

        // When
        val result = repository.insertMemoSync(testMemo)

        // Then
        assertEquals(-1L, result)
    }

    @Test
    fun testRefreshMemos() {
        // refreshMemos is a no-op method, just test it doesn't throw
        repository.refreshMemos()
        // No exception should be thrown
    }

    // Helper methods
    private fun createTestMemo(id: Int, content: String): Memo {
        return Memo().apply {
            this.id = id
            this.content = content
            createdTime = System.currentTimeMillis()
            updatedTime = System.currentTimeMillis()
            hash = content.toByteArray()
            guid = UUID.randomUUID().toString()
            enid = "enid_${UUID.randomUUID()}"
            syncStatus = Memo.NEED_SYNC_UP
            status = "active"
            cursorPosition = 0
            wallId = 1
            order = 1
            lastSyncTime = System.currentTimeMillis()
            attributes = "{}"
        }
    }

    private fun createTestMemoEntity(id: Int, content: String, syncStatus: Int = Memo.NEED_SYNC_UP): MemoEntity {
        return MemoEntity(
            id = id,
            content = content,
            createdTime = System.currentTimeMillis(),
            updatedTime = System.currentTimeMillis(),
            hash = content.toByteArray(),
            guid = UUID.randomUUID().toString(),
            enid = "enid_${UUID.randomUUID()}",
            syncStatus = syncStatus,
            status = "active",
            cursorPosition = 0,
            wallId = 1,
            order = 1,
            lastSyncTime = System.currentTimeMillis(),
            attributes = "{}"
        )
    }
}