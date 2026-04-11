# EverMemo 项目概要架构

EverMemo 是一个简单的 Android 备忘录应用，支持与 Evernote 云服务同步。项目正在从传统的 ContentProvider 架构向现代 MVVM + Room 架构迁移。

## 项目概述

- **核心功能**：本地备忘录管理 + Evernote 双向同步
- **开发语言**：Java 为主，正在迁移到 Kotlin
- **架构演进**：传统 Android 架构 → MVVM + Room 架构
- **同步机制**：通过 Evernote SDK 实现云端同步，支持增量同步和冲突处理

## 架构层次

### 1. UI 层

- **[StartActivity.java](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/evermemo/StartActivity.java)**：主界面，展示多列网格布局的备忘录列表
    - 使用 `MultiColumnListView`（来自 ExGridView 库）
    - 处理列表显示、选择、同步操作
- **[MemoActivity.java](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/evermemo/MemoActivity.java)**：备忘录编辑界面
- **[SettingActivity.java](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/evermemo/SettingActivity.java)**：应用设置界面

### 2. 表示层（现代架构）

- **[MemoViewModel.kt](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/viewmodel/MemoViewModel.kt)**：ViewModel 提供备忘录数据给 UI 组件
    - 使用 `LiveData` 实现响应式数据流
    - 遵循 Android Architecture Components
    - 依赖 `MemoRepository` 进行数据访问

### 3. 数据层（过渡状态）

#### 传统数据层（逐步淘汰）

- **[Memo.java](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/data/Memo.java)**：传统数据模型类
- **[MemoDB.java](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/data/MemoDB.java)**：SQLiteOpenHelper 定义 `Memo` 表结构
- **[MemoProvider.java](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/data/MemoProvider.java)**：ContentProvider 数据访问
- **MemoRepository.java**（已删除）：基于 ContentProvider 的旧仓库

#### 现代数据层（Room-based）

- **[AppDatabase.kt](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/data/AppDatabase.kt)**：Room 数据库单例
    - 使用 `fallbackToDestructiveMigration()`（暂未实现迁移策略）
    - 数据库版本：2
- **[MemoEntity.kt](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/data/MemoEntity.kt)**：Room 实体类
    - 包含 `_id`、`content`、`createdtime`、`updatedtime`、`hash`、`guid`、`enid`、`syncstatus` 等字段
    - 实现 `Parcelable` 支持界面传参
    - 提供与 Evernote `Note` 类型的相互转换方法
- **[MemoDao.kt](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/data/MemoDao.kt)**：数据访问接口
    - 包含 CRUD 操作和同步相关查询
    - 支持 `LiveData` 观察数据变化
- **[MemoRepository.kt](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/repository/MemoRepository.kt)**：现代仓库实现
    - 封装 Room 数据库操作
    - 提供 `LiveData` 给 ViewModel

### 4. 同步层

- **[Evernote.java](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/sync/Evernote.java)**：同步控制器
    - 处理 Evernote 认证、笔记本管理、双向同步
    - 使用 `AsyncTask` 执行后台同步操作
    - 支持增量同步和冲突解决
- **EverNoteEx 库**：自定义 Evernote SDK 包装
    - 位于 `libraries/EverNoteEx/`
    - 包含 `evernote-api-1.25.jar` 和 `scribe-1.3.1.jar`

### 5. 库模块

- **EverNoteEx**：Evernote SDK 封装模块
- **ExGridView**：多列 ListView 带下拉刷新功能

## 技术栈

### Android SDK

- `minSdkVersion`: 21（Android 5.0）
- `targetSdkVersion`: 34（Android 14）
- `compileSdkVersion`: 34（Android 14）
- `buildToolsVersion`: "34.0.0"

### 主要依赖

- **AndroidX**:
    - `androidx.appcompat:appcompat:1.6.1`
    - `androidx.core:core:1.12.0`
    - `androidx.recyclerview:recyclerview:1.3.2`
    - `androidx.lifecycle:lifecycle-viewmodel:2.7.0`
    - `androidx.lifecycle:lifecycle-livedata:2.7.0`
    - `androidx.room:room-runtime:2.6.0`
- **Kotlin**:
    - `org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0`
    - `kotlinx-coroutines-android:1.7.3`
- **第三方服务**:
    - Umeng 统计分析（`com.umeng.umsdk:common:+`）
    - 腾讯 Bugly 崩溃报告（`com.tencent.bugly:crashreport`）

### 构建系统

- **Gradle Wrapper**: 8.6（使用腾讯云镜像）
- **Android Gradle Plugin**: 8.3.2
- **Kotlin Plugin**: 1.9.0

## 数据模型与同步状态

### MemoEntity 关键字段

|字段名|类型|说明|
|---|---|---|
|`_id`|Long|主键，自增|
|`content`|String|备忘录内容|
|`createdtime`/`updatedtime`|Long|创建/更新时间戳|
|`enid`|String|Evernote 云端 ID|
|`syncstatus`|Int|同步状态（0-无需同步，1-需同步上传，3-需同步删除等）|
|`status`|String|本地状态（"common"/"delete"）|
|`hash`|ByteArray|内容哈希，用于同步冲突检测|

### 同步状态机

- `NEED_NOTHING` (0): 无需同步
- `NEED_SYNC_UP` (1): 需要上传到 Evernote
- `NEED_SYNC_DELETE` (3): 需要从 Evernote 删除
- `SYNCING_UP` (4): 正在上传中
- `SYNCING_DOWN` (5): 正在下载中

## 当前迁移状态

根据 Git 状态，项目正处于架构迁移过程中：

### 已完成迁移

- ✅ `AppDatabase.java` → **[AppDatabase.kt](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/data/AppDatabase.kt)**
- ✅ `MemoDao.java` → **[MemoDao.kt](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/data/MemoDao.kt)**
- ✅ `MemoEntity.java` → **[MemoEntity.kt](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/data/MemoEntity.kt)**
- ✅ `MemoViewModel.java` → **[MemoViewModel.kt](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/viewmodel/MemoViewModel.kt)**
- ✅ `MemoRepository.java` → **[MemoRepository.kt](vscode-webview://1rv9ih0ba73ooir09of18b5ucu8ruhkrt1tptdpc7fualo3v2pp9/app/src/main/java/com/zhan_dui/repository/MemoRepository.kt)**

### 待迁移/共存组件

- `RoomMemoRepository.java`（已注释，可能废弃）
- `Memo.java`、`MemoDB.java`、`MemoProvider.java`（传统组件，仍在使用）
- UI 组件（`StartActivity.java` 等）已开始集成 `MemoViewModel`

### 架构特点

1. **混合架构**：UI 层同时使用传统 LoaderManager 和现代 ViewModel
2. **渐进迁移**：新功能使用 Room，旧功能保持 ContentProvider
3. **数据转换**：`MemoEntity` 提供与旧 `Memo` 类的转换方法
4. **同步兼容**：同步层同时支持新旧数据模型

## 重要配置

### Evernote API

- 通过 `BuildConfig` 字段配置：`EVERNOTE_CONSUMER_KEY`、`EVERNOTE_CONSUMER_SECRET`
- 支持生产环境和沙箱环境切换

### 数据库

- 使用 `fallbackToDestructiveMigration()`，数据会在模式变更时丢失
- 需要实现正式迁移策略以确保数据安全

### 中国区域优化

- Maven 仓库使用腾讯云镜像加速
- Umeng 统计分析针对中国市场优化

这个架构体现了从传统 Android 开发模式向现代 Jetpack 架构演进的典型路径，适合作为学习 Android 架构迁移的参考案例。