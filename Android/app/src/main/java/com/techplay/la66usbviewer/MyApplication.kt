package com.techplay.la66usbviewer

import com.hoho.android.usbserial.driver.UsbSerialPort
// import com.tencent.bugly.crashreport.CrashReport

class MyApplication : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        android.util.Log.e("6545", "545454")
        // Removed error reporting as there is no need for this for now
        // CrashReport.initCrashReport(applicationContext, "865b700306", false)
        android.util.Log.e("6545", "54545411")
    }

    companion object {
        var port: UsbSerialPort? = null
    }
}
