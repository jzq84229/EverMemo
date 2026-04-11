plugins {
    id("com.android.library")
}

android {
    resourcePrefix = "esdk_"
    namespace = "com.evernote.androidsdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 14

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lint {
        abortOnError = true

        htmlOutput = file("$project.buildDir/reports/lint/lint.html")
        xmlOutput = file("$project.buildDir/reports/lint/lint.xml")
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    api("com.evernote:evernote-api:1.25.1")
    api("org.scribe:scribe:1.3.7")
    api("net.vrallev.android:android-task:1.1.7")
    api("com.squareup.okhttp:okhttp:2.7.5")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:1.7.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
