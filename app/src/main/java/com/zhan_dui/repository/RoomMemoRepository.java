//package com.zhan_dui.repository;
//
//import android.content.Context;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.Transformations;
//
//import com.zhan_dui.data.AppDatabase;
//import com.zhan_dui.data.Memo;
//import com.zhan_dui.data.MemoDao;
//import com.zhan_dui.data.MemoEntity;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * Room-based repository for memo data operations.
// * Replaces ContentProvider-based MemoRepository with Room database.
// */
//public class RoomMemoRepository {
//    private final MemoDao memoDao;
//    private final ExecutorService executor = Executors.newSingleThreadExecutor();
//    private final LiveData<List<Memo>> allMemos;
//
//    public RoomMemoRepository(Context context) {
//        AppDatabase database = AppDatabase.getInstance(context);
//        this.memoDao = database.memoDao();
//        // Convert Room's LiveData<List<MemoEntity>> to LiveData<List<Memo>>
//        this.allMemos = Transformations.map(memoDao.getAllMemos(), entities -> {
//            if (entities == null) {
//                return new ArrayList<>();
//            }
//            List<Memo> memos = new ArrayList<>();
//            for (MemoEntity entity : entities) {
//                memos.add(entity.toMemo());
//            }
//            return memos;
//        });
//    }
//
//    /**
//     * Get all memos (excluding deleted ones) as LiveData
//     */
//    public LiveData<List<Memo>> getAllMemos() {
//        return allMemos;
//    }
//
////    /**
////     * Get a memo by ID
////     */
////    public Memo getMemoById(int id) {
////        MemoEntity entity = memoDao.getMemoById(id);
////        return entity != null ? entity.toMemo() : null;
////    }
//
//    /**
//     * Insert a new memo
//     */
//    public void insertMemo(Memo memo, InsertCallback callback) {
//        executor.execute(() -> {
//            try {
//                MemoEntity entity = MemoEntity.fromMemo(memo);
//                long id = memoDao.insert(entity);
//                memo.setId((int) id);
//                if (callback != null) {
//                    callback.onInserted(id);
//                }
//                // LiveData will automatically update
//            } catch (Exception e) {
//                if (callback != null) {
//                    callback.onError(e);
//                }
//            }
//        });
//    }
//
//    /**
//     * Update an existing memo
//     */
//    public void updateMemo(Memo memo, UpdateCallback callback) {
//        executor.execute(() -> {
//            try {
//                MemoEntity entity = MemoEntity.fromMemo(memo);
//                int rowsAffected = memoDao.update(entity);
//                if (callback != null) {
//                    callback.onUpdated(rowsAffected > 0);
//                }
//                // LiveData will automatically update
//            } catch (Exception e) {
//                if (callback != null) {
//                    callback.onError(e);
//                }
//            }
//        });
//    }
//
//    /**
//     * Delete a memo (soft delete - mark as deleted)
//     */
//    public void deleteMemo(int id, DeleteCallback callback) {
//        executor.execute(() -> {
//            try {
//                // Mark as deleted with sync status
//                int rowsAffected = memoDao.markAsDeleted(id, Memo.NEED_SYNC_DELETE);
//                if (callback != null) {
//                    callback.onDeleted(rowsAffected > 0);
//                }
//                // LiveData will automatically update
//            } catch (Exception e) {
//                if (callback != null) {
//                    callback.onError(e);
//                }
//            }
//        });
//    }
//
//
//    /**
//     * Refresh memos from database
//     * Note: Room's LiveData automatically refreshes when data changes
//     */
//    public void refreshMemos() {
//        // No-op: LiveData automatically updates
//    }
//
//    /**
//     * Get memos that need synchronization
//     */
//    public List<Memo> getMemosNeedingSync() {
//        List<MemoEntity> entities = memoDao.getMemosNeedingSync();
//        List<Memo> memos = new ArrayList<>();
//        for (MemoEntity entity : entities) {
//            memos.add(entity.toMemo());
//        }
//        return memos;
//    }
//
//    /**
//     * Get a memo by ID
//     */
//    public Memo getMemoById(int id) {
//        try {
//            java.util.concurrent.Future<Memo> future = executor.submit(() -> {
//                MemoEntity entity = memoDao.getMemoById(id);
//                return entity != null ? entity.toMemo() : null;
//            });
//            return future.get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    // Callback interfaces
//    public interface InsertCallback {
//        void onInserted(long id);
//        void onError(Exception e);
//    }
//
//    public interface UpdateCallback {
//        void onUpdated(boolean success);
//        void onError(Exception e);
//    }
//
//    public interface DeleteCallback {
//        void onDeleted(boolean success);
//        void onError(Exception e);
//    }
//
//    /**
//     * Synchronously insert a memo and return its ID.
//     * WARNING: This method blocks the calling thread.
//     * @return the inserted memo ID, or -1 if failed
//     */
//    public long insertMemoSync(Memo memo) {
//        try {
//            java.util.concurrent.Future<Long> future = executor.submit(() -> {
//                MemoEntity entity = MemoEntity.fromMemo(memo);
//                return memoDao.insert(entity);
//            });
//            return future.get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return -1;
//        }
//    }
//
//    /**
//     * Get memo by Evernote ID (enid)
//     */
//    public Memo getMemoByEnid(String enid) {
//        try {
//            java.util.concurrent.Future<Memo> future = executor.submit(() -> {
//                MemoEntity entity = memoDao.getMemoByEnid(enid);
//                return entity != null ? entity.toMemo() : null;
//            });
//            return future.get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    /**
//     * Get all memos including deleted ones (for sync)
//     */
//    public List<Memo> getAllMemosIncludingDeleted() {
//        try {
//            java.util.concurrent.Future<List<Memo>> future = executor.submit(() -> {
//                List<MemoEntity> entities = memoDao.getAllMemosIncludingDeleted();
//                List<Memo> memos = new ArrayList<>();
//                for (MemoEntity entity : entities) {
//                    memos.add(entity.toMemo());
//                }
//                return memos;
//            });
//            return future.get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * Update sync fields after sync operation
//     */
//    public void updateSyncFields(int id, int syncStatus, long updatedTime, byte[] hash, UpdateCallback callback) {
//        executor.execute(() -> {
//            try {
//                int rowsAffected = memoDao.updateSyncFields(id, syncStatus, updatedTime, hash);
//                if (callback != null) {
//                    callback.onUpdated(rowsAffected > 0);
//                }
//            } catch (Exception e) {
//                if (callback != null) {
//                    callback.onError(e);
//                }
//            }
//        });
//    }
//
//    /**
//     * Update after successful sync (with enid)
//     */
//    public void updateAfterSync(int id, int syncStatus, String enid, long updatedTime, byte[] hash, UpdateCallback callback) {
//        executor.execute(() -> {
//            try {
//                int rowsAffected = memoDao.updateAfterSync(id, syncStatus, enid, updatedTime, hash);
//                if (callback != null) {
//                    callback.onUpdated(rowsAffected > 0);
//                }
//            } catch (Exception e) {
//                if (callback != null) {
//                    callback.onError(e);
//                }
//            }
//        });
//    }
//
//    /**
//     * Update sync status only
//     */
//    public void updateSyncStatus(int id, int syncStatus, UpdateCallback callback) {
//        executor.execute(() -> {
//            try {
//                int rowsAffected = memoDao.updateSyncStatus(id, syncStatus);
//                if (callback != null) {
//                    callback.onUpdated(rowsAffected > 0);
//                }
//            } catch (Exception e) {
//                if (callback != null) {
//                    callback.onError(e);
//                }
//            }
//        });
//    }
//
//    /**
//     * Synchronously update after sync (with enid)
//     * @return true if update successful
//     */
//    public boolean updateAfterSyncSync(int id, int syncStatus, String enid, long updatedTime, byte[] hash) {
//        try {
//            java.util.concurrent.Future<Boolean> future = executor.submit(() -> {
//                int rowsAffected = memoDao.updateAfterSync(id, syncStatus, enid, updatedTime, hash);
//                return rowsAffected > 0;
//            });
//            return future.get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    /**
//     * Synchronously update sync fields
//     * @return true if update successful
//     */
//    public boolean updateSyncFieldsSync(int id, int syncStatus, long updatedTime, byte[] hash) {
//        try {
//            java.util.concurrent.Future<Boolean> future = executor.submit(() -> {
//                int rowsAffected = memoDao.updateSyncFields(id, syncStatus, updatedTime, hash);
//                return rowsAffected > 0;
//            });
//            return future.get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    /**
//     * Synchronously update sync status
//     * @return true if update successful
//     */
//    public boolean updateSyncStatusSync(int id, int syncStatus) {
//        try {
//            java.util.concurrent.Future<Boolean> future = executor.submit(() -> {
//                int rowsAffected = memoDao.updateSyncStatus(id, syncStatus);
//                return rowsAffected > 0;
//            });
//            return future.get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//}