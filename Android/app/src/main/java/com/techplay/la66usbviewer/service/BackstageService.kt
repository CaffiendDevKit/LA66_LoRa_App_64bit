package com.techplay.la66usbviewer.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.techplay.la66usbviewer.MyApplication

class BackstageService : Service(), SerialInputOutputManager.Listener {
    private val mBinder: IBinder = LocalBinder()

    private var callback: Callback? = null

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    override fun onNewData(data: ByteArray) {
        callback?.onDataChange(data, 0) // Safely call callback if not null
    }

    override fun onRunError(e: Exception) {
        callback?.onRunError() // Safely call callback if not null
    }

    interface Callback {
        fun onDataChange(buffer: ByteArray?, length: Int)
        fun onRunError()
    }

    inner class LocalBinder : Binder() {
        val service: BackstageService
            get() = this@BackstageService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    fun reStart() {
        usbIoManager?.stop()
        usbIoManager = SerialInputOutputManager(MyApplication.port, this).apply {
            start()
        }
    }

    private var usbIoManager: SerialInputOutputManager? = null

    override fun onCreate() {
        super.onCreate()
        Log.e("onServiceConnected", "onCreate")
        usbIoManager = SerialInputOutputManager(MyApplication.port, this).apply {
            start()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}
