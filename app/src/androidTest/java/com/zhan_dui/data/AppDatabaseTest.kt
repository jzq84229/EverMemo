package com.zhan_dui.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var memoDao: MemoDao

    @Before
    fun setup() {
        // Create an in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries() // Allow main thread for simplicity in tests
            .build()
        memoDao = database.memoDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testDatabaseCreation() {
        // Database should be created successfully
        assertNotNull(database)
        assertNotNull(memoDao)
    }

    @Test
    fun testInsertMemo() = runBlocking {
        val memoEntity = createTestMemoEntity(id = 0) // ID will be auto-generated

        val insertedId = memoDao.insert(memoEntity)

        assertTrue(insertedId > 0)
    }

    @Test
    fun testGetMemoById() = runBlocking {
        val memoEntity = createTestMemoEntity(id = 0)
        val insertedId = memoDao.insert(memoEntity)

        val retrievedEntity = memoDao.getMemoById(insertedId.toInt())

        assertNotNull(retrievedEntity)
        assertEquals(insertedId.toInt(), retrievedEntity.id)
        assertEquals(memoEntity.content, retrievedEntity.content)
        assertEquals(memoEntity.createdTime, retrievedEntity.createdTime)
    }

    @Test
    fun testUpdateMemo() = runBlocking {
        val memoEntity = createTestMemoEntity(id = 0)
        val insertedId = memoDao.insert(memoEntity)

        val retrievedEntity = memoDao.getMemoById(insertedId.toInt())
        retrievedEntity.content = "Updated content"
        retrievedEntity.updatedTime = System.currentTimeMillis()

        val rowsUpdated = memoDao.update(retrievedEntity)

        assertEquals(1, rowsUpdated)

        val updatedEntity = memoDao.getMemoById(insertedId.toInt())
        assertEquals("Updated content", updatedEntity.content)
    }

    @Test
    fun testDeleteMemo() = runBlocking {
        val memoEntity = createTestMemoEntity(id = 0)
        val insertedId = memoDao.insert(memoEntity)

        val rowsDeleted = memoDao.delete(memoDao.getMemoById(insertedId.toInt()))

        assertEquals(1, rowsDeleted)

        val deletedEntity = memoDao.getMemoById(insertedId.toInt())
        assertNull(deletedEntity)
    }

    @Test
    fun testGetAllMemos() = runBlocking {
        // Insert multiple memos
        val memo1 = createTestMemoEntity(id = 0, content = "Memo 1")
        val memo2 = createTestMemoEntity(id = 0, content = "Memo 2")
        val memo3 = createTestMemoEntity(id = 0, content = "Memo 3")

        memoDao.insert(memo1)
        memoDao.insert(memo2)
        memoDao.insert(memo3)

        val allMemos = memoDao.allMemos

        // Use LiveData observer to get actual values
        var memosList: List<MemoEntity>? = null
        allMemos.observeForever { memos ->
            memosList = memos
        }

        // Give some time for LiveData to update
        Thread.sleep(100)

        assertNotNull(memosList)
        assertEquals(3, memosList!!.size)
    }

    @Test
    fun testGetMemosNeedingSync() = runBlocking {
        // Create memos with different sync statuses
        val memo1 = createTestMemoEntity(id = 0, syncStatus = Memo.NEED_NOTHING)
        val memo2 = createTestMemoEntity(id = 0, syncStatus = Memo.NEED_SYNC_UP)
        val memo3 = createTestMemoEntity(id = 0, syncStatus = Memo.NEED_SYNC_DELETE)

        memoDao.insert(memo1)
        memoDao.insert(memo2)
        memoDao.insert(memo3)

        val memosNeedingSync = memoDao.memosNeedingSync

        assertEquals(2, memosNeedingSync.size) // Should have 2 memos needing sync
        assertTrue(memosNeedingSync.all { it.syncStatus != Memo.NEED_NOTHING })
    }

    @Test
    fun testMarkAsDeleted() = runBlocking {
        val memoEntity = createTestMemoEntity(id = 0)
        val insertedId = memoDao.insert(memoEntity)

        val rowsAffected = memoDao.markAsDeleted(insertedId.toInt(), Memo.NEED_SYNC_DELETE)

        assertEquals(1, rowsAffected)

        val deletedEntity = memoDao.getMemoById(insertedId.toInt())
        assertNotNull(deletedEntity)
        assertEquals("delete", deletedEntity.status)
        assertEquals(Memo.NEED_SYNC_DELETE, deletedEntity.syncStatus)
    }

    @Test
    fun testGetAllMemosExcludesDeleted() = runBlocking {
        val activeMemo = createTestMemoEntity(id = 0, content = "Active Memo", status = "active")
        val deletedMemo = createTestMemoEntity(id = 0, content = "Deleted Memo", status = "delete")

        memoDao.insert(activeMemo)
        memoDao.insert(deletedMemo)

        val allMemos = memoDao.allMemos

        var memosList: List<MemoEntity>? = null
        allMemos.observeForever { memos ->
            memosList = memos
        }

        Thread.sleep(100)

        assertNotNull(memosList)
        assertEquals(1, memosList!!.size) // Should only have active memo
        assertEquals("Active Memo", memosList!![0].content)
    }

    private fun createTestMemoEntity(
        id: Int,
        content: String = "Test content",
        syncStatus: Int = Memo.NEED_SYNC_UP,
        status: String = "active"
    ): MemoEntity {
        return MemoEntity(
            id = id,
            content = content,
            createdTime = System.currentTimeMillis(),
            updatedTime = System.currentTimeMillis(),
            hash = content.toByteArray(),
            guid = UUID.randomUUID().toString(),
            enid = "enid_${UUID.randomUUID()}",
            syncStatus = syncStatus,
            status = status,
            cursorPosition = 0,
            wallId = 1,
            order = 1,
            lastSyncTime = System.currentTimeMillis(),
            attributes = "{}"
        )
    }
}