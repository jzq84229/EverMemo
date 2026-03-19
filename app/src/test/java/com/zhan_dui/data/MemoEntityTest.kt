package com.zhan_dui.data

import org.junit.Test
import org.junit.Assert.*
import java.util.*

class MemoEntityTest {

    @Test
    fun testMemoEntityCreation() {
        val id = 1
        val content = "Test content"
        val createdTime = System.currentTimeMillis()
        val updatedTime = System.currentTimeMillis()
        val hash = byteArrayOf(1, 2, 3, 4)
        val guid = UUID.randomUUID().toString()
        val enid = "enid123"
        val syncStatus = Memo.NEED_SYNC_UP
        val status = "active"
        val cursorPosition = 5
        val wallId = 10
        val order = 1
        val lastSyncTime = System.currentTimeMillis()
        val attributes = "{\"key\":\"value\"}"

        val entity = MemoEntity(
            id, content, createdTime, updatedTime, hash,
            guid, enid, syncStatus, status, cursorPosition,
            wallId, order, lastSyncTime, attributes
        )

        assertEquals(id, entity.id)
        assertEquals(content, entity.content)
        assertEquals(createdTime, entity.createdTime)
        assertEquals(updatedTime, entity.updatedTime)
        assertArrayEquals(hash, entity.hash)
        assertEquals(guid, entity.guid)
        assertEquals(enid, entity.enid)
        assertEquals(syncStatus, entity.syncStatus)
        assertEquals(status, entity.status)
        assertEquals(cursorPosition, entity.cursorPosition)
        assertEquals(wallId, entity.wallId)
        assertEquals(order, entity.order)
        assertEquals(lastSyncTime, entity.lastSyncTime)
        assertEquals(attributes, entity.attributes)
    }

    @Test
    fun testMemoEntityDefaultConstructor() {
        val entity = MemoEntity()

        entity.id = 1
        entity.content = "Test"
        entity.createdTime = 1000L
        entity.updatedTime = 2000L
        entity.hash = byteArrayOf(1, 2, 3)
        entity.guid = "guid123"
        entity.enid = "enid456"
        entity.syncStatus = Memo.NEED_NOTHING
        entity.status = "active"
        entity.cursorPosition = 3
        entity.wallId = 7
        entity.order = 2
        entity.lastSyncTime = 3000L
        entity.attributes = "{}"

        assertEquals(1, entity.id)
        assertEquals("Test", entity.content)
        assertEquals(1000L, entity.createdTime)
        assertEquals(2000L, entity.updatedTime)
        assertArrayEquals(byteArrayOf(1, 2, 3), entity.hash)
        assertEquals("guid123", entity.guid)
        assertEquals("enid456", entity.enid)
        assertEquals(Memo.NEED_NOTHING, entity.syncStatus)
        assertEquals("active", entity.status)
        assertEquals(3, entity.cursorPosition)
        assertEquals(7, entity.wallId)
        assertEquals(2, entity.order)
        assertEquals(3000L, entity.lastSyncTime)
        assertEquals("{}", entity.attributes)
    }

    @Test
    fun testFromMemoConversion() {
        val memo = Memo().apply {
            id = 1
            content = "Test content"
            createdTime = 1000L
            updatedTime = 2000L
            hash = byteArrayOf(1, 2, 3)
            guid = "guid123"
            enid = "enid456"
            syncStatus = Memo.NEED_SYNC_UP
            status = "active"
            cursorPosition = 5
            wallId = 10
            order = 1
            lastSyncTime = 3000L
            attributes = "{\"test\":true}"
        }

        val entity = MemoEntity.fromMemo(memo)

        assertEquals(memo.id, entity.id)
        assertEquals(memo.content, entity.content)
        assertEquals(memo.createdTime, entity.createdTime)
        assertEquals(memo.updatedTime, entity.updatedTime)
        assertArrayEquals(memo.hash, entity.hash)
        assertEquals(memo.guid, entity.guid)
        assertEquals(memo.enid, entity.enid)
        assertEquals(memo.syncStatus, entity.syncStatus)
        assertEquals(memo.status, entity.status)
        assertEquals(memo.cursorPosition, entity.cursorPosition)
        assertEquals(memo.wallId, entity.wallId)
        assertEquals(memo.order, entity.order)
        assertEquals(memo.lastSyncTime, entity.lastSyncTime)
        assertEquals(memo.attributes, entity.attributes)
    }

    @Test
    fun testToMemoConversion() {
        val entity = MemoEntity(
            1, "Test content", 1000L, 2000L,
            byteArrayOf(1, 2, 3), "guid123", "enid456",
            Memo.NEED_SYNC_UP, "active", 5, 10, 1,
            3000L, "{\"test\":true}"
        )

        val memo = entity.toMemo()

        assertEquals(entity.id, memo.id)
        assertEquals(entity.content, memo.content)
        assertEquals(entity.createdTime, memo.createdTime)
        assertEquals(entity.updatedTime, memo.updatedTime)
        assertArrayEquals(entity.hash, memo.hash)
        assertEquals(entity.guid, memo.guid)
        assertEquals(entity.enid, memo.enid)
        assertEquals(entity.syncStatus, memo.syncStatus)
        assertEquals(entity.status, memo.status)
        assertEquals(entity.cursorPosition, memo.cursorPosition)
        assertEquals(entity.wallId, memo.wallId)
        assertEquals(entity.order, memo.order)
        assertEquals(entity.lastSyncTime, memo.lastSyncTime)
        assertEquals(entity.attributes, memo.attributes)
    }

    @Test
    fun testRoundTripConversion() {
        val originalMemo = Memo().apply {
            id = 1
            content = "Round trip test"
            createdTime = System.currentTimeMillis()
            updatedTime = System.currentTimeMillis()
            hash = "test".toByteArray()
            guid = UUID.randomUUID().toString()
            enid = "enid_roundtrip"
            syncStatus = Memo.NEED_NOTHING
            status = "active"
            cursorPosition = 10
            wallId = 20
            order = 3
            lastSyncTime = System.currentTimeMillis()
            attributes = "{\"roundtrip\":true}"
        }

        val entity = MemoEntity.fromMemo(originalMemo)
        val convertedMemo = entity.toMemo()

        assertEquals(originalMemo.id, convertedMemo.id)
        assertEquals(originalMemo.content, convertedMemo.content)
        assertEquals(originalMemo.createdTime, convertedMemo.createdTime)
        assertEquals(originalMemo.updatedTime, convertedMemo.updatedTime)
        assertArrayEquals(originalMemo.hash, convertedMemo.hash)
        assertEquals(originalMemo.guid, convertedMemo.guid)
        assertEquals(originalMemo.enid, convertedMemo.enid)
        assertEquals(originalMemo.syncStatus, convertedMemo.syncStatus)
        assertEquals(originalMemo.status, convertedMemo.status)
        assertEquals(originalMemo.cursorPosition, convertedMemo.cursorPosition)
        assertEquals(originalMemo.wallId, convertedMemo.wallId)
        assertEquals(originalMemo.order, convertedMemo.order)
        assertEquals(originalMemo.lastSyncTime, convertedMemo.lastSyncTime)
        assertEquals(originalMemo.attributes, convertedMemo.attributes)
    }
}