package com.zhan_dui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MemoViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockRepository: RoomMemoRepository

    private lateinit var viewModel: TestMemoViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TestMemoViewModel(mockApplication, mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGetAllMemos() = runTest {
        // Given
        val testMemos = listOf(
            createTestMemo(1, "Memo 1"),
            createTestMemo(2, "Memo 2")
        )
        val liveData = MutableLiveData<List<Memo>>().apply {
            value = testMemos
        }
        whenever(mockRepository.getAllMemos()).thenReturn(liveData)

        // When
        val result = viewModel.getAllMemos()

        // Then
        assertNotNull(result)
        assertEquals(testMemos, result.value)
    }

    @Test
    fun testSelectMemo() {
        // Given
        val testMemo = createTestMemo(1, "Selected Memo")

        // When
        viewModel.selectMemo(testMemo)

        // Then
        val selectedMemo = viewModel.getSelectedMemo().value
        assertEquals(testMemo, selectedMemo)
    }

    @Test
    fun testClearSelectedMemo() {
        // Given
        viewModel.selectMemo(createTestMemo(1, "Test"))

        // When
        viewModel.clearSelectedMemo()

        // Then
        assertNull(viewModel.getSelectedMemo().value)
    }

    @Test
    fun testCreateMemo_Success() = runTest {
        // Given
        val testContent = "New memo content"
        val expectedId = 42L
        val expectedUri = Uri.parse("content://com.zhan_dui.evermemo.memoprovider/memo/42")

        whenever(mockRepository.insertMemoSync(any())).thenReturn(expectedId)

        // When
        val result = viewModel.createMemo(testContent)

        // Then
        assertEquals(expectedUri, result)
        assertNull(viewModel.getErrorMessage().value)
        verify(mockRepository).insertMemoSync(argThat {
            content == testContent && createdTime > 0
        })
    }

    @Test
    fun testCreateMemo_EmptyContent() = runTest {
        // Given
        val emptyContent = "   "

        // When
        val result = viewModel.createMemo(emptyContent)

        // Then
        assertNull(result)
        assertEquals("Memo content cannot be empty", viewModel.getErrorMessage().value)
        verify(mockRepository, never()).insertMemoSync(any())
    }

    @Test
    fun testCreateMemo_NullContent() = runTest {
        // When
        val result = viewModel.createMemo(null)

        // Then
        assertNull(result)
        assertEquals("Memo content cannot be empty", viewModel.getErrorMessage().value)
        verify(mockRepository, never()).insertMemoSync(any())
    }

    @Test
    fun testCreateMemo_InsertFailed() = runTest {
        // Given
        val testContent = "Test content"
        whenever(mockRepository.insertMemoSync(any())).thenReturn(-1L)

        // When
        val result = viewModel.createMemo(testContent)

        // Then
        assertNull(result)
        assertEquals("Failed to insert memo", viewModel.getErrorMessage().value)
    }

    @Test
    fun testUpdateMemo_Success() = runTest {
        // Given
        val testMemo = createTestMemo(1, "Updated content")

        doAnswer { invocation ->
            val callback = invocation.getArgument<RoomMemoRepository.UpdateCallback>(1)
            callback.onUpdated(true)
            null
        }.whenever(mockRepository).updateMemo(eq(testMemo), any())

        // When
        val result = viewModel.updateMemo(testMemo)

        // Then
        assertTrue(result)
        assertNull(viewModel.getErrorMessage().value)
        verify(mockRepository).updateMemo(eq(testMemo), any())
    }

    @Test
    fun testUpdateMemo_NullMemo() = runTest {
        // When
        val result = viewModel.updateMemo(null)

        // Then
        assertFalse(result)
        assertEquals("Memo cannot be null", viewModel.getErrorMessage().value)
        verify(mockRepository, never()).updateMemo(any(), any())
    }

    @Test
    fun testUpdateMemo_EmptyContent_DeletesMemo() = runTest {
        // Given
        val testMemo = createTestMemo(1, "")

        doAnswer { invocation ->
            val callback = invocation.getArgument<RoomMemoRepository.DeleteCallback>(1)
            callback.onDeleted(true)
            null
        }.whenever(mockRepository).deleteMemo(eq(1), any())

        // When
        val result = viewModel.updateMemo(testMemo)

        // Then
        assertTrue(result)
        verify(mockRepository).deleteMemo(eq(1), any())
        verify(mockRepository, never()).updateMemo(any(), any())
    }

    @Test
    fun testUpdateMemo_Failed() = runTest {
        // Given
        val testMemo = createTestMemo(1, "Test content")

        doAnswer { invocation ->
            val callback = invocation.getArgument<RoomMemoRepository.UpdateCallback>(1)
            callback.onUpdated(false)
            null
        }.whenever(mockRepository).updateMemo(eq(testMemo), any())

        // When
        val result = viewModel.updateMemo(testMemo)

        // Then
        assertTrue(result) // Returns true immediately
        // Error message should be set via callback
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Failed to update memo", viewModel.getErrorMessage().value)
    }

    @Test
    fun testDeleteMemo() = runTest {
        // Given
        val memoId = 1

        doAnswer { invocation ->
            val callback = invocation.getArgument<RoomMemoRepository.DeleteCallback>(1)
            callback.onDeleted(true)
            null
        }.whenever(mockRepository).deleteMemo(eq(memoId), any())

        // When
        val result = viewModel.deleteMemo(memoId)

        // Then
        assertEquals(1, result)
        verify(mockRepository).deleteMemo(eq(memoId), any())
    }

    @Test
    fun testDeleteMemo_Failed() = runTest {
        // Given
        val memoId = 1

        doAnswer { invocation ->
            val callback = invocation.getArgument<RoomMemoRepository.DeleteCallback>(1)
            callback.onDeleted(false)
            null
        }.whenever(mockRepository).deleteMemo(eq(memoId), any())

        // When
        val result = viewModel.deleteMemo(memoId)

        // Then
        assertEquals(1, result) // Returns 1 immediately
        // Error message should be set via callback
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Failed to delete memo", viewModel.getErrorMessage().value)
    }

    @Test
    fun testRefreshMemos() {
        // When
        viewModel.refreshMemos()

        // Then
        verify(mockRepository).refreshMemos()
    }

    @Test
    fun testGetMemoById() = runTest {
        // Given
        val testMemo = createTestMemo(1, "Test Memo")
        whenever(mockRepository.getMemoById(1)).thenReturn(testMemo)

        // When
        val result = viewModel.getMemoById(1)

        // Then
        assertEquals(testMemo, result)
    }

    @Test
    fun testGetMemoById_NotFound() = runTest {
        // Given
        whenever(mockRepository.getMemoById(999)).thenReturn(null)

        // When
        val result = viewModel.getMemoById(999)

        // Then
        assertNull(result)
    }

    // Test version of MemoViewModel that allows injection
    private class TestMemoViewModel(
        application: Application,
        testRepository: RoomMemoRepository
    ) : MemoViewModel(application) {
        init {
            try {
                // Use reflection to set the private memoRepository field
                val memoRepositoryField = MemoViewModel::class.java.getDeclaredField("memoRepository")
                memoRepositoryField.isAccessible = true
                memoRepositoryField.set(this, testRepository)

                // Also need to set allMemos field to use the test repository's data
                val allMemosField = MemoViewModel::class.java.getDeclaredField("allMemos")
                allMemosField.isAccessible = true
                allMemosField.set(this, testRepository.getAllMemos())

                // Other fields can remain with default values
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
}