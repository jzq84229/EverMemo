# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

- Build the project: `./gradlew clean build` (as per README.md)
- Assemble APK: `./gradlew assemble`
- Lint checks are configured to not abort on errors (`abortOnError false`)

## Architecture Overview

EverMemo is a simple memo app that syncs with Evernote. The codebase is in transition from a traditional Android architecture to a modern MVVM architecture with Room database.

### UI Layer
- **StartActivity**: Main activity displaying memos in a multi-column grid (`MultiColumnListView` from ExGridView library). Handles list display, selection, and sync actions.
- **MemoActivity**: Note editing activity.
- **SettingActivity**: App settings.

### Presentation Layer (Modern)
- **MemoViewModel**: ViewModel that provides memo data to UI components using LiveData. Follows Android Architecture Components.
- Uses **RoomMemoRepository** for data access.

### Data Layer (Transitional State)
The project is migrating from a ContentProvider-based architecture to a Room-based architecture:

#### Legacy Data Layer (Still Present)
- **Memo**: Legacy data model class representing a memo note, with conversion methods to/from Evernote's `Note` type.
- **MemoDB**: SQLiteOpenHelper defining the `Memo` table schema (includes fields: `_id`, `content`, `createdtime`, `updatedtime`, `hash`, `guid`, `enid`, `syncstatus`, etc.).
- **MemoProvider**: ContentProvider for memo data access, used with LoaderManager in StartActivity.
- **MemoRepository**: Legacy repository using ContentProvider API.

#### Modern Data Layer (Room-based)
- **MemoEntity**: Room entity class for the Memo table with annotations (`@Entity`, `@ColumnInfo`).
- **AppDatabase**: Room database singleton with `fallbackToDestructiveMigration()` enabled.
- **MemoDao**: Data Access Object with queries for CRUD operations and LiveData support.
- **RoomMemoRepository**: Modern repository that uses Room database and provides LiveData to ViewModel.

### Sync Layer
- **Evernote**: Main sync controller handles authentication, notebook management, and bidirectional sync with Evernote. Uses AsyncTask for background operations.
- **EverNoteEx library**: Custom wrapper around the Evernote SDK (includes `evernote-api-1.25.jar` and `scribe-1.3.1.jar`).

### Libraries
- **EverNoteEx**: Evernote SDK wrapper module (located in `libraries/EverNoteEx/`).
- **ExGridView**: Multi-column ListView with pull-to-refresh functionality (located in `libraries/ExGridView/`).

### Third-party Integrations
- **Umeng Analytics**: Integrated for usage tracking (see `UMENG_APPKEY` in AndroidManifest.xml).
- **Umeng Update**: For app updates.

## Key Configuration

### Android SDK Versions
- `minSdkVersion`: 21 (Android 5.0)
- `targetSdkVersion`: 34 (Android 14)
- `compileSdkVersion`: 34 (Android 14)
- `buildToolsVersion`: "34.0.0"

### Dependencies
- **AndroidX Libraries**:
  - `androidx.appcompat:appcompat:1.6.1`
  - `androidx.core:core:1.12.0`
  - `androidx.recyclerview:recyclerview:1.3.2`
  - `androidx.constraintlayout:constraintlayout:2.1.4`
  - `androidx.lifecycle:lifecycle-viewmodel:2.7.0`
  - `androidx.lifecycle:lifecycle-livedata:2.7.0`
  - `androidx.room:room-runtime:2.6.0` (with `room-compiler:2.6.0`)
- **Kotlin**: `org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0` and `kotlinx-coroutines-android:1.7.3`
- **Evernote SDK**: Bundled as JAR in EverNoteEx library
- **Umeng SDKs**: `com.umeng.umsdk:common:+`, `com.umeng.umsdk:asms:+`, `com.umeng.umsdk:uyumao:+`, `com.umeng.umsdk:apm:+`
- **Project Structure**: Multi-module with root project and two library modules (defined in `settings.gradle`)

### Build System
- **Gradle Wrapper**: 8.6 (configured in `gradle/wrapper/gradle-wrapper.properties`, using Tencent Cloud mirror)
- **Android Gradle Plugin**: 8.3.2 (modern `plugins { id 'com.android.application' }` syntax)
- **Kotlin Plugin**: `org.jetbrains.kotlin.android` version 1.9.0
- **Repositories**: Uses Tencent Cloud mirrors (`mirrors.cloud.tencent.com`) for faster downloads in China

### Important Files
- `build.gradle` - Root build configuration with SDK versions
- `app/build.gradle` - Main module configuration with dependencies
- `local.properties` - SDK path (user-specific, not versioned)
- `Key` - Keystore for signing (binary file)
- `.travis.yml` - Legacy CI configuration for Travis CI
- `settings.gradle` - Project structure and repository configuration

## Important Notes

1. **Evernote API Keys**: Configured as BuildConfig fields in `app/build.gradle` (`EVERNOTE_CONSUMER_KEY`, `EVERNOTE_CONSUMER_SECRET`).

2. **Architecture Migration**: The project is in transition from ContentProvider-based data layer to Room-based MVVM architecture. Both old and new implementations coexist:
   - Legacy: `MemoDB` (SQLiteOpenHelper), `MemoProvider` (ContentProvider), `MemoRepository`
   - Modern: `AppDatabase` (Room), `MemoEntity`, `MemoDao`, `RoomMemoRepository`, `MemoViewModel`

3. **Database Migration**: Room database uses `fallbackToDestructiveMigration()` - data will be lost on schema changes until proper migrations are implemented.

4. **Chinese Maven Mirrors**: Repository URLs point to Tencent Cloud mirrors for faster access in China. May need adjustment for other regions.

5. **Analytics Integration**: Umeng analytics requires `UMENG_APPKEY` in AndroidManifest.xml (configured via `manifestPlaceholders` in build.gradle).

6. **Sync Mechanism**: Sync uses a combination of local database and Evernote's cloud. Sync status is tracked via `syncstatus` field in Memo table.

7. **Threading**: Room operations use `ExecutorService` for background execution. LiveData automatically updates UI on data changes.

8. **Kotlin Integration**: The project includes Kotlin dependencies but main codebase appears to be Java. Kotlin is available for future development.