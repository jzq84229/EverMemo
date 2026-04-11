package com.zhan_dui.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MemoDao {
    @Query("SELECT * FROM Memo WHERE status != 'delete' ORDER BY updatedtime DESC")
    fun getAllMemos(): LiveData<List<MemoEntity>>

    @Query("SELECT * FROM Memo ORDER By updatedtime DESC")
    fun getAllMemosWithDelete(): MutableList<MemoEntity>


    @Query("SELECT * FROM Memo WHERE _id = :id")
    fun getMemoById(id: Long): MemoEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(memo: MemoEntity): Long

    @Update
    fun update(memo: MemoEntity): Int

//    @Delete
//    fun delete(memo: MemoEntity): Int

    @Query("UPDATE Memo SET status = 'delete', syncstatus = :syncStatus WHERE _id = :id")
    fun markAsDeleted(id: Int, syncStatus: Int): Int

    @Query("SELECT * FROM Memo WHERE syncstatus != 0")
    fun getMemosNeedingSync(): MutableList<MemoEntity>

    @Query("SELECT * FROM Memo WHERE enid = :enid")
    fun getMemoByEnid(enid: String): MutableList<MemoEntity>

    @Query("SELECT * FROM Memo")
    fun  getAllMemosIncludingDeleted(): MutableList<MemoEntity>

    @Query("UPDATE Memo SET syncstatus = :syncStatus, updatedtime = :updatedTime, hash = :hash WHERE _id = :id")
    fun updateSyncFields(id: Int, syncStatus: Int, updatedTime: Long, hash: ByteArray?): Int

    @Query("UPDATE Memo SET syncstatus = :syncStatus, enid = :enid, updatedtime = :updatedTime, hash = :hash WHERE _id = :id")
    fun updateAfterSync(
        id: Int,
        syncStatus: Int,
        enid: String?,
        updatedTime: Long,
        hash: ByteArray?
    ): Int

    @Query("UPDATE Memo SET syncstatus = :syncStatus WHERE _id = :id")
    fun updateSyncStatus(id: Int, syncStatus: Int): Int
}