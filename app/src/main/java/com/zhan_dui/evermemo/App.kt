package com.zhan_dui.evermemo

import android.app.Application
import android.content.Context
import android.os.Build
import com.evernote.client.android.EvernoteSession
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import com.tencent.shiply.integration.ShiplyParams
import com.zhan_dui.utils.SPManager


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initBugly()
        initShiply()
        SPManager.getInstance().init(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
//        Multidex.install(this)
    }

    private fun initBugly() {
        val strategy = UserStrategy(applicationContext)
        strategy.deviceID = "deviceId"
        strategy.deviceModel = Build.MODEL
//        strategy.setAppChannel("myChannel");  //设置渠道
//        strategy.setAppVersion("1.0.1");      //App的版本
//        strategy.setAppPackageName(applicationContext.packageName);  //App的包名
        /* 最新版SDK支持trace文件采集和anr过程中的主线程堆栈信息采集，由于抓取堆栈的系统接口 Thread.getStackTrace 可能造成crash，建议只对少量用户开启 */
        // 设置anr时是否获取系统trace文件，默认为false
        strategy.isEnableCatchAnrTrace = false
        // 设置是否获取anr过程中的主线程堆栈，默认为true
        strategy.isEnableRecordAnrMainStack = false
        /* 由于抓取全部堆栈接口Thread.getAllStackTraces()可能引起crash，建议只对少量用户开启 */
        // crashEnable和anrEnable默认值为true
        CrashReport.setAllThreadStackEnable(applicationContext, false, false)
        CrashReport.setIsDevelopmentDevice(applicationContext, BuildConfig.DEBUG);
        CrashReport.initCrashReport(applicationContext, BuildConfig.BUGLY_ID, BuildConfig.DEBUG, strategy)

    }

    private fun initShiply() {
//        val bundleID: String = BuildConfig.APPLICATION_ID // 宿主的 app 包名
//
//        val isDebugPackage: Boolean = BuildConfig.DEBUG // 宿主是否是debug包
//
//        val shiplyParams: ShiplyParams = ShiplyParams.Builder()
//            .appId("53594dde3e")
//            .appKey("eba6fb38-78c4-4939-8dcc-77abe779c41d")
//            .userId("123321")
//            .deviceId("deviceXXX")
//            .hostAppVersion(BuildConfig.VERSION_NAME) // 宿主的 app 版本
//            .isDebugPackage(isDebugPackage)
//            .devModel(Build.MODEL)
//            .devManufacturer(Build.MANUFACTURER)
//            .androidSystemVersion(Build.VERSION.SDK_INT.toString())
//            .logImpl(CustomLogger()) // 自定义日志实现，建议对接到业务方自己的日志接口
//            .build()
//        // 初始化ShiplyPro SDK，统一接入时必须调用
//        initialize(applicationContext, shiplyParams)
//        // 初始化配置开关SDK，使用配置开关时必须调用
//        getRdeliveryInstance()
    }

}