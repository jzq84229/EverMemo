package com.zhan_dui.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room entity for the Memo table.
 */
@Entity(tableName = "Memo")
public class MemoEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private int id;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "createdtime")
    private long createdTime;

    @ColumnInfo(name = "updatedtime")
    private long updatedTime;

    @ColumnInfo(name = "hash")
    private byte[] hash;

    @ColumnInfo(name = "guid")
    private String guid;

    @ColumnInfo(name = "enid")
    private String enid;

    @ColumnInfo(name = "syncstatus")
    private int syncStatus;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "cursorposition")
    private int cursorPosition;

    @ColumnInfo(name = "wallid")
    private int wallId;

    @ColumnInfo(name = "orderid")
    private int order;

    @ColumnInfo(name = "lastsynctime")
    private long lastSyncTime;

    @ColumnInfo(name = "attributes")
    private String attributes;

    // Constructors
    public MemoEntity() {
    }

    @Ignore
    public MemoEntity(int id, String content, long createdTime, long updatedTime, byte[] hash,
                     String guid, String enid, int syncStatus, String status, int cursorPosition,
                     int wallId, int order, long lastSyncTime, String attributes) {
        this.id = id;
        this.content = content;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.hash = hash;
        this.guid = guid;
        this.enid = enid;
        this.syncStatus = syncStatus;
        this.status = status;
        this.cursorPosition = cursorPosition;
        this.wallId = wallId;
        this.order = order;
        this.lastSyncTime = lastSyncTime;
        this.attributes = attributes;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getEnid() {
        return enid;
    }

    public void setEnid(String enid) {
        this.enid = enid;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(int cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

    public int getWallId() {
        return wallId;
    }

    public void setWallId(int wallId) {
        this.wallId = wallId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    // Conversion methods to/from Memo (legacy model)
    public static MemoEntity fromMemo(Memo memo) {
        return new MemoEntity(
                memo.getId(),
                memo.getContent(),
                memo.getCreatedTime(),
                memo.getUpdatedTime(),
                memo.getHash(),
                memo.getGuid(),
                memo.getEnid(),
                memo.getSyncStatus(),
                memo.getStatus(),
                memo.getCursorPosition(),
                memo.getWallId(),
                memo.getOrder(),
                memo.getLastSyncTime(),
                memo.getAttributes()
        );
    }

    public Memo toMemo() {
        Memo memo = new Memo();
        memo.setId(id);
        memo.setContent(content);
        memo.setCreatedTime(createdTime);
        memo.setUpdatedTime(updatedTime);
        memo.setHash(hash);
        memo.setGuid(guid);
        memo.setEnid(enid);
        memo.setSyncStatus(syncStatus);
        memo.setStatus(status);
        memo.setCursorPosition(cursorPosition);
        memo.setWallId(wallId);
        memo.setOrder(order);
        memo.setLastSyncTime(lastSyncTime);
        memo.setAttributes(attributes);
        return memo;
    }
}