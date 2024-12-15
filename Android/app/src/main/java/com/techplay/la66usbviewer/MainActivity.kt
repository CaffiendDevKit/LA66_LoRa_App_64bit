package com.techplay.la66usbviewer

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager


import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView

import com.techplay.la66usbviewer.bean.LogBean
import com.techplay.la66usbviewer.utils.TextUtils
import com.techplay.la66usbviewer.service.BackstageService
import com.techplay.la66usbviewer.utils.LogUtil
import com.techplay.la66usbviewer.utils.PreferencesUtil


import java.io.IOException
import java.text.DecimalFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity(),
    SerialInputOutputManager.Listener,
    ServiceConnection {

    private lateinit var textStatus1: TextView
    private lateinit var textStatus2: TextView
    private lateinit var textStatus3: TextView
    private lateinit var textStatus31: TextView

    private lateinit var img1: ImageView

    //internal lateinit var listView: ListView
    private lateinit var recyclerView: RecyclerView
    private lateinit var logAdapter: LogAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    val logList: MutableList<LogBean> = mutableListOf()

    private lateinit var btnSwitch: SwitchCompat

    // Register for ActivityResult to handle permissions
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")
        setContentView(R.layout.activity_lan)
        Log.d("MainActivity", "Layout set successfully")
        // Keep the screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        try {
            // Set up window properties
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false) // Adjust insets handling for Android 11+
                window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }

            // Set the status bar color
            val color = ContextCompat.getColor(this, R.color.transparent)
            window.statusBarColor = color
            Log.d("MainActivity", "Window properties configured")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error configuring window properties", e)
        }

        // Load location time preference
        locationTime = PreferencesUtil.getInt(this@MainActivity, "locationTime", locationTime)
        Log.d("MainActivity", "locationTime retrieved: $locationTime")

        // Initialize the permission launcher
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            Log.d("MainActivity", "Permission result: $isGranted")
            if (isGranted) {
                initMap()
            } else {
                showText("Please give location permission manually")
            }
        }

        // Initialize views and other components
        Log.d("MainActivity", "Initializing views")
        initView()
        Log.d("MainActivity", "Views initialized")

        Log.d("MainActivity", "Calling time3()")
        time3()
        Log.d("MainActivity", "time3() completed")

        // Check and request permissions
        Log.d("MainActivity", "Checking permissions")
        checkAndRequestPermissions()
        Log.d("MainActivity", "Permissions check completed")
    }

    private fun checkAndRequestPermissions() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            initMap() // Permission already granted
        } else {
            requestPermissionLauncher.launch(permission) // Request the permission
        }
    }

    fun showText(str: String?) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }


    private val seleteHex = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun initView() {
        Log.d("initView", "Initializing initView")
        btnSwitch = findViewById(R.id.btn_switch)
        Log.d("initView", "Initialized btnSwitch")
        btnSwitch.setOnCheckedChangeListener { _, isChecked ->
            isTime = isChecked
        }
        img1 = findViewById<ImageView>(R.id.img1)
        textStatus1 = findViewById<TextView>(R.id.text_statu1)
        textStatus2 = findViewById<TextView>(R.id.text_statu2)
        textStatus3 = findViewById<TextView>(R.id.text_statu3)
        textStatus31 = findViewById<TextView>(R.id.text_statu31)
        Log.d("initView", "Initialized item group 1")

        findViewById<View>(R.id.btn_Reconnection).setOnClickListener {
            XPopup.Builder(this@MainActivity).asConfirm("Is Connection la66?", "",
                "Canel", "OK",
                {
                    try {
                        getDriver()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, { }, false
            ).show()
            //                initUsb();
        }
        findViewById<View>(R.id.btn_Reconnection1).setOnClickListener {
            XPopup.Builder(this@MainActivity).asConfirm("Check   it is online?", "",
                "Canel", "OK",
                {
                    try {
                        checkConnect()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, { }, false
            ).show()
        }
        Log.d("initView", "Initialized item group 2")

        //        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        // Setting the layout manager for the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // This replaces `isStackFromBottom` for RecyclerView
        }
        // Initialize the adapter
        logAdapter = LogAdapter(this)
        logAdapter.setSelectHex(seleteHex)
        logAdapter.addResult(logList)
        // Attach the adapter to the RecyclerView
        recyclerView.adapter = logAdapter

// To mimic `TRANSCRIPT_MODE_ALWAYS_SCROLL`, add a scroll listener
        recyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (logList.isNotEmpty()) {
                recyclerView.scrollToPosition(logAdapter.itemCount - 1)
            }
        }
        Log.d("initView", "Initialized listView")
        //        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
    //            @Override
    //            public void onRefresh() {
    //                //刷新需执行的操作
    ////                setble();
    //            }
    //        });
    //        swipeRefreshLayout.setRefreshing(false);
        findViewById<View>(R.id.btn_send_hex).setOnClickListener { selectPopup5!!.show() }
        findViewById<View>(R.id.btn_send_code).setOnClickListener { sendCode() }
        editTextTextPersonName = findViewById<EditText>(R.id.editTextTextPersonName)
        val btn_send_hex = findViewById<TextView>(R.id.btn_send_hex)
        btn_send_hex.setText(
            resources.getString(R.string.text_code_send) + "(" + resources.getString(
                R.string.ascii
            ) + ")"
        )
        selectPopup5 = XPopup.Builder(this)
            .autoDismiss(false)
            .asCenterList(
                resources.getString(R.string.text_code_send), arrayOf<String>(
                    resources.getString(R.string.ascii), resources.getString(R.string.hex)
                )
            ) { position, text ->
                selectPopup5!!.dismiss()
                if (position == 0) {
                    sendHex = false
                    btn_send_hex.setText(
                        resources.getString(R.string.text_code_send) + "(" + resources.getString(
                            R.string.ascii
                        ) + ")"
                    )
                } else {
                    sendHex = true
                    btn_send_hex.setText(
                        resources.getString(R.string.text_code_send) + "(" + resources.getString(
                            R.string.hex
                        ) + ")"
                    )
                }
            }
        findViewById<View>(R.id.btn_save).setOnClickListener {
            XPopup.Builder(this@MainActivity).asConfirm("Exit APP", "",
                "Canel", "OK",
                { System.exit(0) }, { }, false
            ).show()
        }
        findViewById<View>(R.id.btn_clear).setOnClickListener {
            XPopup.Builder(this@MainActivity)
                .asConfirm("", resources.getString(R.string.isclearlog),
                    "Canel", "OK",
                    { clearLog() }, { }, false
                ).show()
        }
        val btn_location = findViewById<TextView>(R.id.btn_location)
        btn_location.setText("Uplink Interval（" + locationTime + "s）")

        findViewById<View>(R.id.btn_location1).setOnClickListener {
            val popup = XPopup.Builder(this@MainActivity).asInputConfirm(
                "Please enter the positioning interval (S)",
                ""
            ) { text ->
                try {
                    val newInterval = text.toInt()
                    locationTime = newInterval

                    // Save the new interval in shared preferences
                    PreferencesUtil.putInt(this@MainActivity, "locationTime", locationTime)

                    // Update the UI
                    btn_location.text = "Uplink Interval ($locationTime s)"

                    // Stop and restart location updates with the new interval
                    stopLocationUpdates()
                    updateLocationRequestInterval(locationTime * 1000L)
                    startLocationUpdates()
                } catch (e: Exception) {
                    Log.e("LocationUpdate", "Invalid input for location interval", e)
                }
            }

            val etInput = popup.findViewById<EditText>(R.id.et_input)
            etInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

            val tvConfirm = popup.findViewById<TextView>(R.id.tv_confirm)
            tvConfirm.text = "Ok"

            val tvCancel = popup.findViewById<TextView>(R.id.tv_cancel)
            tvCancel.text = "Cancel"

            popup.show()
        }

    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            performLogSaving()
        } else {
            Toast.makeText(
                this@MainActivity,
                resources.getText(R.string.permission_denied), // Add a string resource for "Permission denied"
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun saveLog() {
        val perms = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (perms.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            performLogSaving() // Permissions already granted, proceed with log saving
        } else {
            // Request permissions
            requestPermissionsLauncher.launch(perms)
        }
    }

    private fun performLogSaving() {
        if (logList.isNotEmpty()) {
            val log = logList.joinToString(separator = "") { it.text.toString() }
            LogUtil.writerlog(log)
        } else {
            Toast.makeText(
                this@MainActivity,
                resources.getText(R.string.log_Tips),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun sendCode() {
        if (editTextTextPersonName!!.text.toString().trim { it <= ' ' }.length == 0) {
            Toast.makeText(this, resources.getText(R.string.instructions), Toast.LENGTH_SHORT)
                .show()
        } else {
            if (sendHex) {
                send(TextUtils.decode(editTextTextPersonName!!.text.toString().trim { it <= ' ' }))
            } else {
                send(editTextTextPersonName!!.text.toString().trim { it <= ' ' })
            }
        }
    }

    private var sendtime: Long = 0
    private var isSendData = false

    fun send(send: String) {
        try {
            timeNum = 0
            Log.e("tyyy", "$send*")
            Log.e("tyyy", send.indexOf("SENDB").toString() + "*")
            Log.e("tyyy", "$send*$isSendData")
            if (isSendData) {
                if (send.indexOf("SENDB") != -1) {
                    return
                }
                if (sendDataList.size == 0) {
    //                    showText("请在发包，待发包结束后自动发送");
                    Log.e(
                        "tyyy",
                        send + "*" + send + "Please send it automatically after the contract is awarded"
                    )
                    sendtime = Date().time
                    sendDataList.add(send)
                    if (send.indexOf("NJS") == -1) runOnUiThread {
                        showText("Please send it automatically after the contract is awarded")
                    }
                } else {
    //                    showText("存在待发送指令，请稍后");
                    Log.e("tyyy", "$send*$isSendData")
                    Log.e(
                        "tyyy",
                        send + "*" + send + "There are instructions to be sent, please wait：" + isSendData
                    )
                    if (send.indexOf("NJS") == -1) runOnUiThread {
                        showText("There are instructions to be sent, please wait")
                    }
                }
                return
            }

            Log.e("TextUtils", send)
            val hex1 = TextUtils.strToASCII(send) + "0D0A"
            Log.e("TAg", hex1)

            val logBean = LogBean()
            logBean.text = (send + TextUtils.decode("0D0A"))
            logBean.time = (Date().time)
            logBean.type = (2)
            logList.add(logBean)
            runOnUiThread {
                logAdapter.addResult(logList)
                logAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(logAdapter.itemCount - 1)
            }
            if (isConnect) {
                val bytes = hex1.chunked(2)
                    .map { it.toInt(16).toByte() }
                    .toByteArray()
                MyApplication.port!!.write(bytes, 3000)
                if (send.indexOf("SENDB") != -1) {
                    isSendData = true
                }
            }
        } catch (e: Exception) {
            Log.e("Exception", "send")
            e.printStackTrace()
        }
    }

    private var editTextTextPersonName: EditText? = null
    private var btn_send_hex: TextView? = null
    private var btn_location: TextView? = null
    private var locationTime = 60
    private var sendHex = false
    private var selectPopup5: BasePopupView? = null
    var mUsbManager: UsbManager? = null
    var UsbDevice: UsbDevice? = null
    var isConnect: Boolean = false
    var isLan: Boolean = false
    var isLocation: Boolean = false
    var isTime: Boolean = false

    fun clearLog() {
        logList.clear()
        logAdapter.addResult(logList)
        logAdapter.notifyDataSetChanged()
    }

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    val result: List<UsbDevice> = ArrayList()
    var driver: UsbSerialDriver? = null
    var manager1: UsbManager? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Throws(Exception::class)
    fun getDriver() {
        // Find all available drivers from attached devices.
        manager1 = getSystemService(USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager1)
        if (availableDrivers.isEmpty()) {
            return
        }

        // Open a connection to the first available driver.
        driver = availableDrivers[0]
        if (manager1!!.hasPermission(driver!!.device)) {
            getConnection()
        } else {
            val filter = IntentFilter()
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            filter.addAction(ACTION_USB_PERMISSION)
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
            val mPermissionIntent = PendingIntent.getBroadcast(
                this, 0,
                Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
            )
            manager1!!.requestPermission(driver!!.device, mPermissionIntent)
        }
    }

    private var connection: UsbDeviceConnection? = null

    @Throws(Exception::class)
    fun getConnection() {
        connection = manager1!!.openDevice(driver!!.device)
        //        connection.controlTransfer()
        if (connection == null) {
            Log.e("UsbDeviceConnection", "manager.openDevice(driver.getDevice())")
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return
        }
        Log.e("UsbDeviceConnection123", "manager.openDevice(driver.getDevice())")
        if (MyApplication.port != null) {
            MyApplication.port!!.close()
        }

        driver?.ports?.get(0)?.let { port ->
            MyApplication.port = port
            port.open(connection)
            port.setParameters(
                9600,
                8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )
        } ?: run {
            // Handle the null case here, e.g., show an error message
            showText("No available ports.")
        }

        success()
        Log.e("UsbDeviceConnection", "success")
        myService?.reStart()
            ?: bindService(Intent(this, BackstageService::class.java), this, BIND_AUTO_CREATE)


        //        Log.e("TAH", ":" + usbIoManager.getReadBufferSize());
    //        Log.e("TAH", ":" + usbIoManager.getWriteBufferSize());

    //       usbIoManager.setReadTimeout(3000);
    //       usbIoManager.setWriteTimeout(3000);
        time1()
        time2()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("onDestroy", "onDestroy")
        MyApplication.port = null

        myService?.onDestroy()
        myService = null
    }

private var myService: BackstageService? = null

    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        Log.e("onServiceConnected", "onServiceConnected")
        val binder = service as BackstageService.LocalBinder
        myService = binder.service
        myService?.setCallback(object : BackstageService.Callback {
            override fun onDataChange(buffer: ByteArray?, length: Int) {
                val recv = buffer?.joinToString(separator = " ") { byte ->
                    String.format("%02X", byte)
                } ?: ""
                str += recv

                startTime = System.currentTimeMillis()
                if (isSendData && (sendTime - startTime > 5000)) {
                    runOnUiThread {
                        textStatus31.text = ""
                        img1.setImageDrawable(null)
                    }
                } else if (isSendData && (sendTime - startTime > 7000)) {
                    isSendData = false
                    sendTime = 0
                    if (sendDataList.isNotEmpty()) {
                        send(sendDataList.last())
                        sendDataList.removeAt(sendDataList.size - 1)
                    }
                }
            }

            override fun onRunError() {
                runOnUiThread {
                    textStatus1.text = "LA66 Not Detect"
                    isConnect = false
                    textStatus2.text = "LoRaWAN：Offline"
                    isSendData = false
                    isLan = false
                }
            }
        })
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                val isPermissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                val usbDevice: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice!!::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                }

                Log.e("BroadcastReceiver", "Received USB permission intent")

                if (isPermissionGranted && usbDevice != null) {
                    try {
                        if (driver?.device == usbDevice) {
                            getConnection()
                        } else {
                            error("Device mismatch or not recognized")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        error("Failed to establish connection: ${e.message}")
                    }
                } else {
                    error("Permission denied or device null")
                }

                // Always unregister receiver to prevent memory leaks
                try {
                    context.unregisterReceiver(this)
                } catch (e: IllegalArgumentException) {
                    Log.e("BroadcastReceiver", "Receiver already unregistered: ${e.message}")
                }
            }
        }
    }


    fun success() {
        runOnUiThread {
            textStatus1.setText("LA66 Detected")
            isConnect = true
        }
    }

    fun error() {
    //        runOnUiThread(new Runnable() {
    //            @Override
    //            public void run() {
    //                text_statu1.setText("LA66 Not Detect");
    //                isConnect = false;
    //            }
    //        });
    }

    var timer1: Timer? = null

    var timer2: Timer? = null
    var timer3: Timer? = null

    fun time3() {
        if (timer3 != null) {
            timer3 = null
            timer3 = Timer()
        } else {
            timer3 = Timer()
        }
        timer3!!.schedule(object : TimerTask() {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun run() {
                if (!isConnect) {
                    try {
                        Log.e("isConnect", "schedule")
                        getDriver()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }, (1000 * 1).toLong(), (1000 * 3).toLong())
    }

    fun time2() {
        if (timer2 != null) {
            timer2 = null
            timer2 = Timer()
        } else {
            timer2 = Timer()
        }
        timer2!!.schedule(object : TimerTask() {
            override fun run() {
                try {
                    checkConnect()
                    //                    new readThread().start();
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }, (1000 * 1).toLong(), (1000 * 30).toLong())
    }

    private var timeNum = 0
    fun time1() {
        if (timer1 != null) {
            timer1!!.cancel()
            timer1 = null
            timer1 = Timer()
        } else {
            timer1 = Timer()
        }
        timer1!!.schedule(object : TimerTask() {
            override fun run() {
                endTime = Date().time

                if (endTime > startTime + 100 && startTime != 0L) {
                    runOnUiThread {
                        try {
                            Log.e("schedule", str)
                            //                            Log.e("ttt", str.length() + "**");
                            val stt = str.replace(" ", "").uppercase(Locale.getDefault())

                            //                            Log.e("ttt", stt + "**");
    //                            Log.e("ttt", TextUtils.decode(stt));
                            if (stt.contains("527373693D20")) {
                                timeNum = 0
                                val str1 =
                                    stt.split("527373693D202D".toRegex())
                                        .dropLastWhile { it.isEmpty() }
                                        .toTypedArray()[1].split("0D".toRegex())
                                        .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                                val str =
                                    stt.split("527373693D202D".toRegex())
                                        .dropLastWhile { it.isEmpty() }
                                        .toTypedArray()[1].split("0D".toRegex())
                                        .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                                Log.e("***777", str)
                                Log.e("***777", TextUtils.decode(str))
                                Log.e("***7771", "$sendTime*")
                                //                                text_statu3.setText("LoRaWAN RSSI:" + TextUtils.decode("2D" + str));
                                val rssi = str.toIntOrNull() ?: 0 // Safely convert string to integer with fallback to 0
                                val drawableRes = when {
                                    rssi > 129 -> R.mipmap.img_rssi1
                                    rssi > 109 -> R.mipmap.img_rssi2
                                    rssi > 90 -> R.mipmap.img_rssi3
                                    rssi > 70 -> R.mipmap.img_rssi4
                                    else -> R.mipmap.img_rssi5
                                }
                                img1.setImageDrawable(ContextCompat.getDrawable(applicationContext, drawableRes))
                                if (dataList.size > 0) dataList.removeAt(dataList.size - 1)
                                sendTime = 0
                                isSendData = false
                                //                                dataList.clear();
                                Log.e("datalist", dataList.size.toString() + "**")
                                if (dataList.size > 0) {
                                    send("AT+SENDB=01,02," + dataList[dataList.size - 1].length / 2 + "," + dataList[dataList.size - 1])
                                } else {
                                    if (sendDataList.size > 0) {
                                        send(sendDataList[sendDataList.size - 1])
                                        sendDataList.removeAt(sendDataList.size - 1)
                                    }
                                }
                            }
                            if (stt.contains("0D0A0D0A4F4B0D0A") && stt.length == 18) {
                                timeNum = 0
                                if (stt.split("0D0A0D0A4F4B0D0A".toRegex())
                                        .dropLastWhile { it.isEmpty() }.toTypedArray()[0].equals(
                                        "31",
                                        ignoreCase = true
                                    )
                                ) {
                                    textStatus2.setText("LoRaWAN：Online")
                                    if (!isLan) {
                                        isLan = true
                                        //                                        send("AT+JOIN?");
                                    }
                                    //                                            send("AT+RSSI=?");
                                } else {
                                    textStatus2.setText("LoRaWAN：Offline")
                                    isLan = false
                                    isSendData = false
                                }
                            }
                            Log.e("sttschedule", stt)
                            Log.e("sttschedule", ":" + stt.indexOf("727854696d656f7574"))

                            if (stt.indexOf("727854696d656f7574") != -1 || stt.indexOf("727854696D656F7574") != -1 || stt.indexOf(
                                    "41545F425553595F4552524F50"
                                ) != -1 || stt.indexOf("41545f425553595f4552524f50") != -1
                            ) {
                                sendTime = 0
                                isSendData = false
                                timeNum++
                                if (timeNum == 2) {
                                    runOnUiThread {
                                        textStatus31.setText("")
                                        img1.setImageDrawable(null)
                                    }
                                }
                            }
                            val logBean = LogBean()
                            logBean.text = (TextUtils.decode(stt))
                            logBean.time = (Date().time)
                            logBean.type = (1)
                            logList.add(logBean)
                            logAdapter.addResult(logList)
                            logAdapter.notifyDataSetChanged()

                            recyclerView.scrollToPosition(logAdapter.itemCount - 1)

                            startTime = 0
                            str = ""
                            Log.e("Exception", "notifyDataSetChanged:" + logList.size)
                            Log.e("Exception", "notifyDataSetChanged:" + (logAdapter.itemCount - 1))
                            Log.e("Exception", "notifyDataSetChanged")
                        } catch (e: Exception) {
                            Log.e("Exception", "Exceptiontime1")
                            e.printStackTrace()
                            startTime = 0
                            str = ""
                        }
                    }
                }
                if (endTime > sendTime + 7000 && sendTime != 0L && rssiData.length != 0) {
                    Log.e("***777sendTime", "$sendTime*")
                    Log.e("***777sendTime", "$endTime*")
                    //                    if(dataList.size()==100){
    //                        dataList.remove(0);
    //                    }
    //                    dataList.add(rssiData);
    //                    sendTime=0;
    //                    rssiData="";
                }
            }
        }, (1000 * 2).toLong(), (1000 * 1 + 100).toLong())
    }

    @Throws(IOException::class)
    fun checkConnect() {
        send("AT+NJS=?")

    //        String hex1 = TextUtils.strToASCII("AT+NJS=?") + "0D0A";
    //        Log.e("TAg", hex1);
    ////        if (manager != null)
    ////            manager.write(HexUtil.hexStringToBytes(hex1));
    //        if (port == null) {
    //            return;
    //        }
    //        port.write(HexUtil.hexStringToBytes(hex1), 3000);

    //        byte[] data = new byte[64];
    //        Log.e("checkConnect", ":" + port.getRTS());
    //        Log.e("checkConnect", ":" + port.getDTR());
    //        int length = 0;
    //        length = port.read(data, 3000);
    //
    //        if (length > 0) {
    //            Message msg = Message.obtain();
    //            String recv = HexUtil.formatHexString(data, true);
    //            msg.obj = recv;
    //            handler.sendMessage(msg);
    //        }
    }


    private var startTime: Long = 0
    private var endTime: Long = 0
    private var str = ""

    override fun onNewData(data: ByteArray?) {
    }

    override fun onRunError(e: Exception?) {
        Log.e("onRunError", "onRunError")
        error()
    }


    private val TAG = "LanActivity"

    private var latitude = 0.0
    private var longitude = 0.0

    private var sendTime: Long = 0
    private val dataList = mutableListOf<String>()
    private val sendDataList = mutableListOf<String>()
    private var rssiData = ""

    fun initLocationClient() {
        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Create a LocationRequest
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L // Default interval for location updates (1 second)
        )
            .setMinUpdateIntervalMillis(1000L)
            .setMaxUpdateDelayMillis(5000L)
            .build()

        // Create a LocationCallback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    latitude = location.latitude
                    longitude = location.longitude

                    Log.e(TAG, "Latitude: $latitude")
                    Log.e(TAG, "Longitude: $longitude")

                    if (!isLocation && latitude != 0.0 && isLan) {
                        val data = generateData(latitude, longitude)
                        sendTime = System.currentTimeMillis()
                        rssiData = data
                        dataList.add(rssiData)
                        send("AT+SENDB=01,02,${data.length / 2},$data")
                    }
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                Log.e(TAG, "Location available: ${locationAvailability.isLocationAvailable}")
            }
        }
    }

    private fun generateData(lat: Double, lon: Double): String {
        val df = DecimalFormat("0")
        val latitudeHex = TextUtils.integerToHexString(df.format(lat * 1_000_000).toInt())
        val longitudeHex = TextUtils.integerToHexString(df.format(lon * 1_000_000).toInt())
        val timestampHex = if (isTime) {
            val timestamp = System.currentTimeMillis() / 1000
            val hex = java.lang.Long.toHexString(timestamp)
            if (hex.length % 2 == 0) hex else "0$hex"
        } else ""

        return latitudeHex + longitudeHex + timestampHex
    }


    private fun initMap() {
        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Create the LocationRequest
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationTime * 1000L)
            .setWaitForAccurateLocation(false) // No need to wait for the most accurate fix
            .setMinUpdateIntervalMillis(1000L) // Minimum interval between updates
            .setMaxUpdateDelayMillis(20000L) // Maximum delay for location updates
            .build()

        // Define the LocationCallback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    // Handle the location object here
                    Log.d("Location", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                Log.d("Location", "Is location available: ${locationAvailability.isLocationAvailable}")
            }
        }

        // Start location updates
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.e("Location", "Permission not granted: ${e.message}")
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateLocationRequestInterval(interval: Long) {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
            .setMinUpdateIntervalMillis(1000L)
            .build()
    }



    class LogAdapter(private val context: Context) : RecyclerView.Adapter<LogAdapter.ViewHolder>() {

        private var selectHex: Boolean = false
        private val logList: MutableList<LogBean> = mutableListOf()

        // Setter for selectHex
        fun setSelectHex(selectHex: Boolean) {
            this.selectHex = selectHex
        }

        // Add new results to the adapter and refresh
        fun addResult(characteristicList: List<LogBean>) {
            logList.clear()
            logList.addAll(characteristicList)
            notifyDataSetChanged()
        }

        // Clear the log list
        fun clear() {
            logList.clear()
            notifyDataSetChanged()
        }

        // ViewHolder to cache view references
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtLog: TextView = itemView.findViewById(R.id.txt_log)
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.adapter_log, parent, false)
            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val logBean = logList[position]
            val text = if (selectHex) TextUtils.strToASCII(logBean.text) else logBean.text
            holder.txtLog.text = text

            val textColor = if (logBean.type == 1) {
                ContextCompat.getColor(context, R.color.black)
            } else {
                ContextCompat.getColor(context, R.color.red)
            }
            holder.txtLog.setTextColor(textColor)
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount(): Int = logList.size
    }

}
