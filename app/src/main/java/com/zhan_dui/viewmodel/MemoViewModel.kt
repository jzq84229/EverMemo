package com.zhan_dui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zhan_dui.data.MemoEntity
import com.zhan_dui.repository.MemoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation

/**
 * ViewModel for memo-related UI data.
 */
open class MemoViewModel(application: Application) : AndroidViewModel(application) {
    private val memoRepository: MemoRepository = MemoRepository(application)
//    private val allMemos: LiveData<List<MemoEntity>>
    private val selectedMemo: MutableLiveData<MemoEntity?> = MutableLiveData<MemoEntity?>()
    private val errorMessage = MutableLiveData<String?>()

//    init {
//        allMemos = memoRepository.getAllMemos()
//    }

    fun getAllMemos(): LiveData<List<MemoEntity>> {
        return memoRepository.getAllMemos()
    }

    fun getSelectedMemo(): LiveData<MemoEntity?> {
        return selectedMemo
    }

    fun getErrorMessage(): LiveData<String?> {
        return errorMessage
    }

    /**
     * Select a memo for editing/viewing
     */
    fun selectMemo(memo: MemoEntity?) {
        selectedMemo.value = memo
    }

    /**
     * Clear selected memo
     */
    fun clearSelectedMemo() {
        selectedMemo.value = null
    }

    /**
     * Create a new memo
     */
    fun createMemo(memo: MemoEntity): Long {
        if (memo.content == null || memo.content?.trim { it <= ' ' }?.isEmpty() == true) {
            errorMessage.value = "Memo content cannot be empty"
            return -1L
        }

        return try {
            val id = memoRepository.insertMemo(memo)
            if (id <= 0) {
                errorMessage.postValue("Failed to insert memo")
            }
            id
        } catch (e: Exception) {
            errorMessage.postValue("Error: " + e.message)
            -1L
        }
    }

    /**
     * Update an existing memo
     */
    fun updateMemo(memo: MemoEntity?) {
        if (memo == null) {
            errorMessage.setValue("Memo cannot be null")
            return
        }

        if (memo.content == null || memo.content?.trim { it <= ' ' }?.isEmpty() == true) {
            // Empty content means delete
            deleteMemo(memo)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rows = memoRepository.updateMemo(memo)
                if (rows <= 0) {
                    errorMessage.postValue("Failed to update memo")
                }
            } catch (e: Exception) {
                errorMessage.postValue("Error: " + e.message)
            }
            null
        }
    }

    /**
     * Delete a memo
     */
    fun deleteMemo(memo: MemoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rows = memoRepository.deleteMemo(memo)
                if (rows <= 0) {
                    errorMessage.postValue("Failed to delete memo")
                }
            } catch (e: Exception) {
                errorMessage.postValue("Error: " + e.message)
            }
            null
        }
    }

    /**
     * Refresh memos from repository
     */
    fun refreshMemos() {
        memoRepository.refreshMemos()
    }

    /**
     * Get memo by ID
     */
    fun getMemoById(id: Long): MemoEntity? {
        return memoRepository.getMemoById(id)
    }
}