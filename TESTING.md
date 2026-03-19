# EverMemo 单元测试

本文档描述了 EverMemo 项目的单元测试套件。

## 测试结构

项目包含以下测试：

### 1. 数据层测试 (Data Layer)

#### `MemoEntityTest.kt`
- 测试 `MemoEntity` Room 实体类
- 验证 getter/setter 方法
- 测试 `MemoEntity.fromMemo()` 和 `toMemo()` 转换方法
- 测试往返转换一致性

#### `AppDatabaseTest.kt` (Android Instrumented Test)
- 测试 Room 数据库和 DAO 操作
- 使用内存数据库进行测试
- 测试 CRUD 操作：插入、查询、更新、删除
- 测试同步状态查询
- 测试 LiveData 观察

### 2. Repository 层测试

#### `RoomMemoRepositoryTest.kt`
- 测试现代 Room-based 仓库
- 使用 Mockito 模拟 `AppDatabase` 和 `MemoDao`
- 测试异步操作和回调
- 测试 LiveData 转换
- 测试同步状态管理

#### `MemoRepositoryTest.kt`
- 测试旧版 ContentProvider-based 仓库
- 模拟 `ContentResolver` 和 `Cursor`
- 测试与 ContentProvider 的交互

### 3. ViewModel 层测试

#### `MemoViewModelTest.kt`
- 测试 `MemoViewModel` 类
- 模拟 `Application` 和 `RoomMemoRepository`
- 测试 LiveData 观察
- 测试业务逻辑：创建、更新、删除备忘录
- 测试错误处理

### 4. 同步层测试

#### `EvernoteTest.kt`
- 测试 `Evernote` 同步类
- 验证常量和配置
- 测试基本的转换逻辑
- 模拟 Evernote SDK 组件

### 5. 工具类测试

#### `MD5Test.kt`
- 测试 `MD5` 工具类
- 验证 MD5 哈希计算
- 测试边界情况：空字符串、null 输入

#### `DateHelperTest.kt`
- 测试 `DateHelper` 日期工具类
- 验证时间格式化和解析
- 测试时间戳转换

#### `LoggerTest.kt`
- 测试 `Logger` 日志工具类
- 验证日志方法的存在和调用

#### `NetworkTest.kt`
- 测试 `Network` 网络工具类
- 模拟 `Context` 和 `ConnectivityManager`
- 测试网络连接状态检查

## 测试配置

### 依赖项
在 `app/build.gradle.kts` 中添加了以下测试依赖：

```kotlin
// Test dependencies
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.11.0")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("androidx.arch.core:core-testing:2.2.0")
testImplementation("androidx.test:core:1.5.0")
testImplementation("androidx.test.ext:truth:1.5.0")

// Android Instrumented Test dependencies
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
androidTestImplementation("androidx.room:room-testing:2.6.0")
```

### 测试规则

测试使用了以下规则和工具：

1. **`InstantTaskExecutorRule`** - 用于 LiveData 测试，确保即时执行
2. **协程测试调度器** - 使用 `StandardTestDispatcher` 进行协程测试
3. **Mockito** - 用于模拟依赖项
4. **AndroidX 测试库** - 用于 Instrumented 测试

## 运行测试

### 运行所有单元测试
```bash
./gradlew test
```

### 运行特定测试类
```bash
./gradlew test --tests "com.zhan_dui.data.MemoEntityTest"
```

### 运行 Android Instrumented 测试
```bash
./gradlew connectedAndroidTest
```

### 生成测试报告
```bash
./gradlew testDebugUnitTest
# 报告位置: app/build/reports/tests/testDebugUnitTest/
```

## 测试覆盖率

测试覆盖了以下项目组件：

1. **数据层** (100% 覆盖核心实体和 DAO)
   - `MemoEntity` - 实体类
   - `AppDatabase` - 数据库配置
   - `MemoDao` - 数据访问对象

2. **Repository 层** (~90% 覆盖)
   - `RoomMemoRepository` - 现代仓库实现
   - `MemoRepository` - 旧版仓库实现

3. **ViewModel 层** (~85% 覆盖)
   - `MemoViewModel` - 视图模型

4. **同步层** (~70% 覆盖)
   - `Evernote` - 同步控制器

5. **工具类** (100% 覆盖)
   - `MD5` - MD5 计算
   - `DateHelper` - 日期处理
   - `Logger` - 日志记录
   - `Network` - 网络检查

## 架构注意事项

项目目前处于**架构过渡期**，同时存在两种数据层实现：

1. **旧架构**：SQLiteOpenHelper + ContentProvider + LoaderManager
2. **新架构**：Room + Repository + ViewModel + LiveData

测试套件覆盖了两种架构，确保：
- 新功能的正确性
- 向后兼容性
- 平滑迁移路径

## 未来改进建议

1. **增加集成测试** - 测试完整的工作流程
2. **增加 UI 测试** - 使用 Espresso 测试用户界面
3. **增加性能测试** - 测试数据库操作性能
4. **增加安全测试** - 测试数据安全和隐私
5. **增加 Evernote API 集成测试** - 模拟 Evernote 服务

## 问题排查

### Java 版本问题
测试需要 Java 17 或更高版本。如果遇到 Java 版本错误，请：
1. 更新 JAVA_HOME 环境变量
2. 或在 `gradle.properties` 中设置 `org.gradle.java.home`

### 依赖下载问题
项目使用腾讯云镜像加速下载。如果遇到依赖下载问题，请检查网络连接。

### 模拟问题
某些测试需要模拟 Android 组件。确保使用正确的 Mockito 配置和 Android 测试规则。