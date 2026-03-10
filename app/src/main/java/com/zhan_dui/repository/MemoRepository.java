package com.zhan_dui.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.zhan_dui.data.Memo;
import com.zhan_dui.data.MemoDB;
import com.zhan_dui.data.MemoProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for memo data operations.
 * Encapsulates data access from ContentProvider and provides LiveData for UI observation.
 */
public class MemoRepository {
    private final ContentResolver contentResolver;
    private final MutableLiveData<List<Memo>> memosLiveData = new MutableLiveData<>();

    public MemoRepository(Context context) {
        this.contentResolver = context.getContentResolver();
    }

    /**
     * Get all memos (excluding deleted ones) as LiveData
     */
    public LiveData<List<Memo>> getAllMemos() {
        loadMemos();
        return memosLiveData;
    }

    /**
     * Load memos from database and update LiveData
     */
    private void loadMemos() {
        new Thread(() -> {
            Cursor cursor = contentResolver.query(
                    MemoProvider.MEMO_URI,
                    null,
                    null,
                    null,
                    MemoDB.UPDATEDTIME + " desc"
            );

            List<Memo> memos = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    memos.add(new Memo(cursor));
                }
                cursor.close();
            }

            memosLiveData.postValue(memos);
        }).start();
    }

    /**
     * Get a memo by ID
     */
    public Memo getMemoById(int id) {
        Cursor cursor = contentResolver.query(
                MemoProvider.MEMO_URI,
                null,
                MemoDB.ID + "=?",
                new String[]{String.valueOf(id)},
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            Memo memo = new Memo(cursor);
            cursor.close();
            return memo;
        }
        return null;
    }

    /**
     * Insert a new memo
     */
    public Uri insertMemo(Memo memo) {
        ContentValues values = memo.toInsertContentValues();
        values.put(MemoDB.SYNCSTATUS, Memo.NEED_SYNC_UP);
        return contentResolver.insert(MemoProvider.MEMO_URI, values);
    }

    /**
     * Update an existing memo
     */
    public int updateMemo(Memo memo) {
        ContentValues values = memo.toUpdateContentValues();
        values.put(MemoDB.SYNCSTATUS, Memo.NEED_SYNC_UP);
        return contentResolver.update(
                MemoProvider.MEMO_URI,
                values,
                MemoDB.ID + "=?",
                new String[]{String.valueOf(memo.getId())}
        );
    }

    /**
     * Delete a memo (soft delete - mark as deleted)
     */
    public int deleteMemo(int id) {
        ContentValues values = new ContentValues();
        values.put(MemoDB.STATUS, Memo.STATUS_DELETE);
        values.put(MemoDB.SYNCSTATUS, Memo.NEED_SYNC_DELETE);
        return contentResolver.update(
                MemoProvider.MEMO_URI,
                values,
                MemoDB.ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    /**
     * Refresh memos from database
     */
    public void refreshMemos() {
        loadMemos();
    }
}