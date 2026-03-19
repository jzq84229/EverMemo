import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}



val props = Properties()
val inputStream = project.rootProject.file("custom.properties").inputStream()
props.load(inputStream)

val evernoteConsumerKey: String = props.getProperty("evernoteConsumerKey")
val evernoteConsumerSecret: String = props.getProperty("evernoteConsumerSecret")
val buglyId: String = props.getProperty("buglyId")
val storePw: String = props.getProperty("storePw")
val keyPw: String = props.getProperty("keyPw")

fun getSvnRevision(): Int {
//    val options = SVNWCUtil.createDefaultOptions(true)
//    val clientManager = SVNClientManager.newInstance(options)
//    val statusClient = clientManager.statusClient
//    val status = statusClient.doStatus(project.rootDir, false)
//    val revision = status.committedRevision.number.toInt()
//    println("project and revision: ${project.rootProject.name}, $revision")
//    return revision
    return 1
}

android {
    namespace = "com.zhan_dui.evermemo"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.zhan_dui.evermemo"
        minSdk = 24
        targetSdk = 34
        versionCode = 7
        versionName = "1.1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
//            // 设置支持的SO库架构
//            abiFilters 'armeabi' //, 'x86', 'armeabi-v7a', 'x86_64', 'arm64-v8a'
            abiFilters.addAll(arrayOf("armeabi", "armeabi-v7a", "arm64-v8a"))
        }
//        buildConfigField("String", "EVERNOTE_CONSUMER_KEY", "\"milkliker\"")
//        buildConfigField("String", "EVERNOTE_CONSUMER_SECRET", "\"f479109c186d284b\"")
        buildConfigField("String", "EVERNOTE_CONSUMER_KEY", "\"${evernoteConsumerKey}\"")
        buildConfigField("String", "EVERNOTE_CONSUMER_SECRET", "\"${evernoteConsumerSecret}\"")
        buildConfigField("String", "BUGLY_ID", "\"${buglyId}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    lint {
        abortOnError = false
    }

    signingConfigs {
        create("release") {
            storeFile = file("../signature/keyStore.jks")
            storePassword = "$storePw"
            keyAlias = "EverMemo"
            keyPassword = "s$keyPw"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    applicationVariants.all {
        val buildType = this.buildType.name
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                outputFileName = "EverMemo_${defaultConfig.versionName}.${defaultConfig.versionCode}_${buildType}_${getTimeNow()}.apk"
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

fun getTimeNow(): String {
    return SimpleDateFormat("yyyyMMddHHmmss").format(Date())
}

dependencies {
    implementation(fileTree("libs") {
        include("*.jar")
    })
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.7.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.7.0")
    implementation("androidx.room:room-runtime:2.6.0")
    annotationProcessor("androidx.room:room-compiler:2.6.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(project(":libraries:EverNoteEx"))
    implementation(project(":libraries:ExGridView"))
//    // 友盟基础组件库（所有友盟业务SDK都依赖基础组件库
//    implementation  'com.umeng.umsdk:common:+'// 必选
//    implementation  'com.umeng.umsdk:asms:+'// 必选
//
//    implementation 'com.umeng.umsdk:uyumao:+' // 高级运营分析功能依赖库，使用U-App卸载分析、开启反作弊能力请务必集成。common需搭配v9.6.3及以上版本，asms需搭配v1.7.0及以上版本。需更新隐私声明。
//    implementation 'com.umeng.umsdk:apm:+'// U-APM产品包依赖，必选

    implementation("com.tencent.bugly:crashreport:4.1.9.3") //其中latest.release指代最新Bugly SDK版本号，也可以指定明确的版本号，例如4.0.3
//    implementation("com.tencent.rqd:nativecrashreport:latest.release") //其中latest.release指代最新Bugly NDK版本号，也可以指定明确的版本号，例如3.9.2

    // ShiplyPro SDK,统一接入时必须依赖
    implementation("com.tencent.shiply:shiplyintegration:1.0.0")
    // Shiply 配置开关 SDK,使用配置开关时必须依赖
    implementation("com.tencent.shiply:rdelivery:1.3.38-RC01")

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
}