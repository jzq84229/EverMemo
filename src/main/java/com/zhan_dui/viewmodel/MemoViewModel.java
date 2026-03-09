package com.zhan_dui.viewmodel;

import android.app.Application;
import android.content.ContentUris;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.zhan_dui.data.Memo;
import com.zhan_dui.data.MemoProvider;
import com.zhan_dui.repository.RoomMemoRepository;

import java.util.List;

/**
 * ViewModel for memo-related UI data.
 */
public class MemoViewModel extends AndroidViewModel {
    private final RoomMemoRepository memoRepository;
    private final LiveData<List<Memo>> allMemos;
    private final MutableLiveData<Memo> selectedMemo = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MemoViewModel(@NonNull Application application) {
        super(application);
        memoRepository = new RoomMemoRepository(application);
        allMemos = memoRepository.getAllMemos();
    }

    public LiveData<List<Memo>> getAllMemos() {
        return allMemos;
    }

    public LiveData<Memo> getSelectedMemo() {
        return selectedMemo;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Select a memo for editing/viewing
     */
    public void selectMemo(Memo memo) {
        selectedMemo.setValue(memo);
    }

    /**
     * Clear selected memo
     */
    public void clearSelectedMemo() {
        selectedMemo.setValue(null);
    }

    /**
     * Create a new memo
     */
    public Uri createMemo(String content) {
        if (content == null || content.trim().isEmpty()) {
            errorMessage.setValue("Memo content cannot be empty");
            return null;
        }

        Memo memo = new Memo();
        memo.setContent(content);
        long id = memoRepository.insertMemoSync(memo);
        if (id > 0) {
            // Return a ContentProvider-style URI for compatibility
            return ContentUris.withAppendedId(MemoProvider.MEMO_URI, id);
        } else {
            errorMessage.setValue("Failed to insert memo");
            return null;
        }
    }

    /**
     * Update an existing memo
     */
    public boolean updateMemo(Memo memo) {
        if (memo == null) {
            errorMessage.setValue("Memo cannot be null");
            return false;
        }

        if (memo.getContent() == null || memo.getContent().trim().isEmpty()) {
            // Empty content means delete
            return deleteMemo(memo.getId()) > 0;
        }

        memoRepository.updateMemo(memo, new RoomMemoRepository.UpdateCallback() {
            @Override
            public void onUpdated(boolean success) {
                if (!success) {
                    errorMessage.setValue("Failed to update memo");
                }
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue("Failed to update memo: " + e.getMessage());
            }
        });
        // Return true immediately assuming operation will succeed
        return true;
    }

    /**
     * Delete a memo
     */
    public int deleteMemo(int id) {
        memoRepository.deleteMemo(id, new RoomMemoRepository.DeleteCallback() {
            @Override
            public void onDeleted(boolean success) {
                if (!success) {
                    errorMessage.setValue("Failed to delete memo");
                }
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue("Failed to delete memo: " + e.getMessage());
            }
        });
        // Return 1 immediately assuming operation will succeed
        return 1;
    }

    /**
     * Refresh memos from repository
     */
    public void refreshMemos() {
        memoRepository.refreshMemos();
    }

    /**
     * Get memo by ID
     */
    public Memo getMemoById(int id) {
        return memoRepository.getMemoById(id);
    }
}