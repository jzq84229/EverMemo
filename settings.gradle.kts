pluginManagement {
    repositories {
        maven {setUrl("https://maven.aliyun.com/repository/google")}
        maven {setUrl("https://maven.aliyun.com/repository/public")}
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {setUrl("https://maven.aliyun.com/repository/google")}
        maven {setUrl("https://maven.aliyun.com/repository/public")}
        google()
        mavenCentral()
        maven {setUrl("https://maven.cnb.cool/tencent-tds/shiply-public/-/packages/")}
    }
}

rootProject.name = "EverMemo"
include(":app")
include(":libraries:EverNoteEx")
include(":libraries:ExGridView")
