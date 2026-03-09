# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

- Build the project: `./gradlew clean build` (as per README.md)
- Assemble APK: `./gradlew assemble`
- Lint checks are configured to not abort on errors (`abortOnError false`)

## Architecture Overview

EverMemo is a simple memo app that syncs with Evernote. The codebase follows a traditional Android architecture with these key components:

### UI Layer
- **StartActivity**: Main activity displaying memos in a multi-column grid (`MultiColumnListView` from ExGridView library). Handles list display, selection, and sync actions.
- **MemoActivity**: Note editing activity.
- **SettingActivity**: App settings.

### Data Layer
- **Memo**: Data model class representing a memo note, with conversion methods to/from Evernote's `Note` type.
- **MemoDB**: SQLiteOpenHelper defining the `Memo` table schema (includes fields: `_id`, `content`, `createdtime`, `updatedtime`, `hash`, `guid`, `enid`, `syncstatus`, etc.).
- **MemoProvider**: ContentProvider for memo data access, used with LoaderManager in StartActivity.

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
- `minSdkVersion`: 9 (Android 2.3)
- `targetSdkVersion`: 18 (Android 4.3)
- `compileSdkVersion`: 19 (Android 4.4)
- `buildToolsVersion`: "19.1.0"

### Dependencies
- **Support Libraries**: `com.android.support:support-v4:19.+` and `appcompat-v7:19.+`
- **Evernote SDK**: Bundled as JAR in EverNoteEx library
- **Project Structure**: Multi-module with root project and two library modules (defined in `settings.gradle`)

### Build System
- **Gradle Wrapper**: 7.4 (configured in `gradle/wrapper/gradle-wrapper.properties`)
- **Android Gradle Plugin**: 7.3.0 (note: still uses legacy `apply plugin: 'android'` syntax)
- **Repositories**: Uses Aliyun mirrors (`maven.aliyun.com`) for faster downloads in China

### Important Files
- `build.gradle` - Root build configuration
- `local.properties` - SDK path (user-specific, not versioned)
- `Key` - Keystore for signing (binary file)
- `.travis.yml` - Legacy CI configuration for Travis CI

## Important Notes

1. **Evernote API Keys**: Hardcoded in `Evernote.java` (CONSUMER_KEY, CONSUMER_SECRET). These may need to be replaced for production use.

2. **Legacy Build Syntax**: The project uses `apply plugin: 'android'` instead of the modern `com.android.application` plugin, despite using AGP 7.3.0. This may cause compatibility issues.

3. **Chinese Maven Mirrors**: Repository URLs point to `maven.aliyun.com` for faster access in China. May need adjustment for other regions.

4. **Analytics Integration**: Umeng analytics requires `UMENG_APPKEY` in AndroidManifest.xml (currently set to a sample key).

5. **Sync Mechanism**: Sync uses a combination of local SQLite database and Evernote's cloud. Sync status is tracked via `syncstatus` field in Memo table.

6. **Recent Changes**: The AGP version was recently updated to 7.3.0 (commit "modify AGP version"), but the plugin syntax remains legacy.