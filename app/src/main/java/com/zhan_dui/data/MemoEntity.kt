package com.zhan_dui.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.evernote.client.android.EvernoteUtil
import com.evernote.edam.type.Note
import com.zhan_dui.evermemo.Constants
import com.zhan_dui.utils.Logger
import kotlinx.parcelize.Parcelize

/**
 * Room entity for the Memo table.
 */
@Entity(tableName = "Memo")
@Parcelize
data class MemoEntity(
    @JvmField
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var _id: Long? = null,
    @JvmField
    @ColumnInfo(name = "guid")
    var guid: String? = null,

    @JvmField
    @ColumnInfo(name = "enid")
    var enid: String? = null,

    @JvmField
    @ColumnInfo(name = "wallid")
    var wallId: Int = 0,

    @JvmField
    @ColumnInfo(name = "orderid")
    var order: Int = 0,

    @JvmField
    @ColumnInfo(name = "content")
    var content: String? = "",

    @JvmField
    @ColumnInfo(name = "attributes")
    var attributes: String? = null,

    @ColumnInfo(name = "status")
    var status: String? = null,

    @JvmField
    @ColumnInfo(name = "hash")
    var hash: ByteArray? = null,

    @JvmField
    @ColumnInfo(name = "syncstatus")
    var syncStatus: Int = 0,

    @JvmField
    @ColumnInfo(name = "createdtime")
    var createdTime: Long = System.currentTimeMillis(),

    @JvmField
    @ColumnInfo(name = "updatedtime")
    var updatedTime: Long = System.currentTimeMillis(),

    @JvmField
    @ColumnInfo(name = "lastsynctime")
    var lastSyncTime: Long = 0,

    @ColumnInfo(name = "cursorposition")
    var cursorPosition: Int = 0
) : Parcelable {
    fun toNote(notebookGuid: String?): Note {
        val note = toNote()
        note.notebookGuid = notebookGuid
        return note
    }

    fun toNote(): Note {
        return Note().apply {
            title = Constants.NOTEBOOK_TITLE
            content = convertContentToEvernote()
        }
    }

    fun toUpdateNote(): Note {
        val note = toNote()
        note.guid = enid
        return note
    }

    fun toDeleteNote(): Note {
        val note = Note()
        note.guid = enid
        return note
    }

    private fun convertContentToEvernote(): String {
//        val evernoteContent = (EvernoteUtil.NOTE_PREFIX
//                + content?.replace("<br>", "<br/>")
//                + EvernoteUtil.NOTE_SUFFIX)
        val evernoteContent = content?.replace("<br>", "<br/>") ?: ""
        Logger.e(LogTag, "同步文字:$evernoteContent")
        return evernoteContent
    }

    fun setContent(content: String?): MemoEntity {
        this.content = content
        Logger.e("下载下来的文本：$content")
        this.status = STATUS_COMMON
        this.order = 0
        this.attributes = ""
        return this
    }

    fun setNeedSyncDelete() {
        syncStatus = NEED_SYNC_DELETE
    }

    fun setNeedSyncUp() {
        syncStatus = NEED_SYNC_UP
    }

    fun isNeedSyncUp(): Boolean {
        if (syncStatus == NEED_SYNC_UP) {
            return true
        } else {
            return false
        }
    }

    fun isSyncingUp(): Boolean {
        if (syncStatus == SYNCING_UP) {
            return true
        } else {
            return false
        }
    }

    fun isNeedSyncDelete(): Boolean {
        if (syncStatus == NEED_SYNC_DELETE) {
            return true
        } else {
            return false
        }
    }

    fun isDeleted(): Boolean {
        if (status == STATUS_DELETE) {
            return true
        } else {
            return false
        }
    }

    fun buildFromNote(note: Note) {
        content = note.content
        hash = note.contentHash
        updatedTime = note.updated
        createdTime = note.created
        enid = note.guid
        syncStatus = NEED_NOTHING
        status = STATUS_COMMON
        cursorPosition = 0
    }

    companion object {
        const val STATUS_DELETE: String = "delete"
        const val STATUS_COMMON: String = "common"

        private const val LogTag = "Memo"

        /**
         * need to do nothing
         */
        const val NEED_NOTHING: Int = 0

        /**
         * need to sync in Evernote
         */
        const val NEED_SYNC_UP: Int = 1

        /**
         * need to delete in Evernote
         */
        const val NEED_SYNC_DELETE: Int = 3

        /**
         * syning up
         */
        const val SYNCING_UP: Int = 4

        /**
         * syning down
         */
        const val SYNCING_DOWN: Int = 5

        @JvmStatic
        fun buildInsertMemoFromNote(note: Note): MemoEntity {
            return MemoEntity(
                content = note.content,
                hash = note.contentHash,
                updatedTime = note.updated,
                createdTime = note.created,
                enid = note.guid,
                syncStatus = NEED_NOTHING,
                status = STATUS_COMMON,
                cursorPosition = 0,
            )
        }

        @JvmStatic
        fun buildUpdateMemoFromNote(note: Note, id: Long): MemoEntity {
            return buildInsertMemoFromNote(note).apply {
                this._id = id
            }
        }
    }
}