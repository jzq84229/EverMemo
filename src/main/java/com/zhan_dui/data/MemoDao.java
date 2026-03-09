package com.zhan_dui.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MemoDao {
    @Query("SELECT * FROM Memo WHERE status != 'delete' ORDER BY updatedtime DESC")
    LiveData<List<MemoEntity>> getAllMemos();

    @Query("SELECT * FROM Memo WHERE _id = :id")
    MemoEntity getMemoById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MemoEntity memo);

    @Update
    int update(MemoEntity memo);

    @Delete
    int delete(MemoEntity memo);

    @Query("UPDATE Memo SET status = 'delete', syncstatus = :syncStatus WHERE _id = :id")
    int markAsDeleted(int id, int syncStatus);

    @Query("SELECT * FROM Memo WHERE syncstatus != 0")
    List<MemoEntity> getMemosNeedingSync();
}