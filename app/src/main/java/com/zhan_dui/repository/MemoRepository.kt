package com.zhan_dui.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.zhan_dui.data.AppDatabase
import com.zhan_dui.data.MemoDao
import com.zhan_dui.data.MemoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for memo data operations.
 * Encapsulates data access from ContentProvider and provides LiveData for UI observation.
 */
class MemoRepository(context: Context) {

    private val memoDao: MemoDao by lazy {
        AppDatabase.getInstance(context).memoDao()
    }

    /**
     * Refresh memos from database
     */
    fun refreshMemos(): LiveData<List<MemoEntity>> {
        return getAllMemos()
    }

    fun getAllMemos(): LiveData<List<MemoEntity>> {
        return memoDao.getAllMemos()
    }

    fun getAllMemosWithDelete(): MutableList<MemoEntity> {
        return memoDao.getAllMemosWithDelete()
    }

    fun getMemoById(id: Long): MemoEntity? {
        return memoDao.getMemoById(id)
    }

    fun getMemoByEnid(enid: String): MutableList<MemoEntity> {
        return memoDao.getMemoByEnid(enid)
    }

    fun insertMemo(memo: MemoEntity): Long {
        return memoDao.insert(memo)
    }

    fun updateMemo(memo: MemoEntity): Int {
        memo.syncStatus = MemoEntity.NEED_SYNC_UP
        return memoDao.update(memo)
    }
    /**
     * Delete a memo (soft delete - mark as deleted)
     */
    fun deleteMemo(memo: MemoEntity): Int{
        memo.status = MemoEntity.STATUS_DELETE
        memo.syncStatus = MemoEntity.NEED_SYNC_DELETE
        return memoDao.update(memo)
    }
}