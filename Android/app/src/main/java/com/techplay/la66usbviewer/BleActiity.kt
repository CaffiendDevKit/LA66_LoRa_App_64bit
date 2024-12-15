package com.techplay.la66usbviewer

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.BarUtils
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.OnConfirmListener
import com.techplay.la66usbviewer.adapter.IndexFragmentPageAdapter
import com.techplay.la66usbviewer.bean.MessageEvent
import com.techplay.la66usbviewer.config.EventBusId
import com.techplay.la66usbviewer.fragment.ConfigFragment
import com.techplay.la66usbviewer.fragment.DeviceInfoFragment
import com.techplay.la66usbviewer.fragment.HomeFragment
import com.techplay.la66usbviewer.fragment.LogFragment
import com.techplay.la66usbviewer.utils.PreferencesUtil
import com.techplay.la66usbviewer.utils.TextUtils

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.security.MessageDigest
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BleActiity : FragmentActivity() {
    private var loadingPopup: BasePopupView? = null
    private var isReset: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.transparent))

        loadingPopup = XPopup.Builder(this).asLoading()
        setContentView(R.layout.activity_ble)

        /* Refactored to make use of kotlin .apply
        BleManager.getInstance().init(application)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .setOperateTimeout(5000
         */
        BleManager.getInstance().apply {
            init(application)
            enableLog(true)
            setReConnectCount(1, 5000)
            setOperateTimeout(5000)
        }

        checkAndRequestPermissions()
        initView()

        /* Replaced with more concise and more kotlin centric code
        val isReset: Boolean = PreferencesUtil.getBoolean(baseContext, "isReset", false)
        EventBus.getDefault().register(this)
        if (isReset) {
            PreferencesUtil.putBoolean(baseContext, "isReset", false)
            var mac = ""
            var name = ""
            if ((mybleDevice == null) or !isConnect) {
            } else {
                name = if (mybleDevice.getName() != null) mybleDevice.getName() else ""
                mac = mybleDevice.getMac()
            }
            val intent: Intent = Intent(
                this,
                SearchActivity::class.java
            )
            intent.putExtra("mac", mac)
            intent.putExtra("name", name)
            startActivity(intent)
            Log.e("PreferencesUtil", "PreferencesUtil2221")
        } else {
        }
        */

        // Check if reset flag is true
        if (PreferencesUtil.getBoolean(baseContext, "isReset", false)) {
            PreferencesUtil.putBoolean(baseContext, "isReset", false)

            val mac = mybleDevice?.mac ?: ""
            val name = mybleDevice?.name ?: ""

            SearchActivity.start(this, mac, name)

            Log.e("PreferencesUtil", "Reset triggered and SearchActivity started")
        }

        EventBus.getDefault().register(this)



        val TAG = "Environment"
        val filePath = Environment.getExternalStorageDirectory().absolutePath
        Log.e(TAG, "exists:$filePath")
        val dir = File(filePath, "ajjText11111")
        Log.e(TAG, "exists:" + dir.path)
        if (!dir.exists()) {
            Log.e(TAG, "exists")
            val bn = dir.mkdir()
            Log.e(TAG, "$bn**")
        }
        Log.e(TAG, dir.exists().toString() + "**")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showLoading(event: MessageEvent<String?>) {
        if (event.id != EventBusId.showLoading) {
            return
        }
        //        MessageEvent<String> messageEvent = new MessageEvent<>();
//        messageEvent.setId(EventBusId.showLoading);
//        EventBus.getDefault().post(messageEvent);
        loadingPopup?.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun dissLoading(event: MessageEvent<String?>) {
        if (event.id != EventBusId.dissLoading) {
            return
        }
        //        MessageEvent<String> messageEvent = new MessageEvent<>();
//        messageEvent.setId(EventBusId.dissLoading);
//        EventBus.getDefault().post(messageEvent);
        loadingPopup?.dismiss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onWeChatLoginSuccess(event: MessageEvent<String?>) {
        if (event.id != EventBusId.STEP) {
            return
        }
    }

    var device: BleDevice? = null

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDclick(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.BleDevice) return

        device = event.body



        device?.let {
            Log.e("Device Info", "MAC: ${it.getMac()}, Key: ${it.getKey()}")

            if (it.getName() == null) {
                Toast.makeText(
                    this,
                    getString(R.string.unsupported_device_message),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }
        //        if (!device.getName().equalsIgnoreCase("BT24-M")) {
//            Toast.makeText(BleActiity.this, "暂不支持连接该设备", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if ((mybleDevice == null) or !isConnect) {
            val messageEvent1: MessageEvent<BleDevice> = MessageEvent<BleDevice>()
            messageEvent1.id = EventBusId.onLink
            EventBus.getDefault().post(messageEvent1)
            //            new XPopup.Builder(BleActiity.this).asConfirm("发现设备", "是否连接该设备", new OnConfirmListener() {
//                @Override
//                public void onConfirm() {
//                    mybleDevice = device;
//                    Log.e("qewq", mybleDevice.getMac());
////                    Log.e("qewq", mybleDevice.getName() );
//                    Log.e("qewq", mybleDevice.getKey());
//                    link(mybleDevice);
//                }
//            }).show();
            return
        }
        device?.let { currentDevice ->
            if (mybleDevice?.mac == currentDevice.mac ) {
                Toast.makeText(this, getString(R.string.device_already_connected), Toast.LENGTH_SHORT).show()
            } else {
                // Post the event to EventBus
                EventBus.getDefault().post(
                    MessageEvent<BleDevice>().apply {
                        id = EventBusId.onreLink
                    }
                )

                // Show the confirmation popup
                XPopup.Builder(this)
                    .asConfirm(
                        getString(R.string.new_device_found),
                        getString(R.string.reconnect_to_new_device)
                    ) {
                        BleManager.getInstance().disconnect(mybleDevice)
                        mybleDevice = currentDevice
                        link(mybleDevice)
                    }.show()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun disconnect(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.disconnect) {
            return
        }
        BleManager.getInstance().disconnect(mybleDevice)
        mybleDevice = null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLinkMain(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.onLinkMain) {
            return
        }
        mybleDevice = device
        link(mybleDevice)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onreLinkMain(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.onreLinkMain) {
            return
        }
        BleManager.getInstance().disconnect(mybleDevice)
        mybleDevice = device
        link(mybleDevice)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun dislink(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.dislink) {
            return
        }
        if (mybleDevice == null) {
            Toast.makeText(this, getResources().getText(R.string.isConnect), Toast.LENGTH_SHORT)
                .show()
            return
        }
        XPopup.Builder(this@BleActiity).asConfirm(
            getResources().getString(R.string.onlink),
            getResources().getString(R.string.unlink),
            object : OnConfirmListener {
                override fun onConfirm() {
                    BleManager.getInstance().disconnect(mybleDevice)
                    mybleDevice = null
                    val messageEvent: MessageEvent<BleDevice> = MessageEvent<BleDevice>()
                    messageEvent.id = EventBusId.UODATE1
                    PreferencesUtil.putBoolean(this@BleActiity, "auto", false)
                    EventBus.getDefault().post(messageEvent)
                }
            }).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun auto(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.auto) {
            return
        }
        device = event.body
        mybleDevice = device
        link(mybleDevice)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun goSearch(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.goSearch) return

        Log.e("PreferencesUtil", "PreferencesUtil1")

        val mac = mybleDevice?.takeIf { isConnect }?.mac ?: ""
        val name = mybleDevice?.takeIf { isConnect }?.name ?: ""

        // Use the new start function
        SearchActivity.start(this, mac, name)
    }

    private lateinit var viewPager: ViewPager2
    private lateinit var radioIndex: RadioGroup

    private lateinit var fragmentList: List<Fragment>
    private lateinit var indexFragmentPageAdapter: IndexFragmentPageAdapter


    fun initView() {
        // Initialize Views
        viewPager = findViewById(R.id.view_pager)
        radioIndex = findViewById(R.id.radio_index)

        // Initialize Fragment List
        fragmentList = mutableListOf(
            HomeFragment(),
            DeviceInfoFragment(),
            ConfigFragment(),
            LogFragment()
        )

        /* Replaced with ViewPager2
        indexFragmentPageAdapter = IndexFragmentPageAdapter(
            supportFragmentManager,
            lifecycle,
            fragmentList
        )*/

        // Configure ViewPager2
        viewPager.apply {
            isUserInputEnabled = false // Disable user swipe
            adapter = indexFragmentPageAdapter
            offscreenPageLimit = 3
            setCurrentItem(0, false)
        }

        // Initialize Listener
        initListener()

        // Log Current Timestamp
        logTimestamp()

        // Initialize Bluetooth
        setBle()
    }

    private fun logTimestamp() {
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        Log.e("timestamp", timestamp)

        try {
            val hash1 = MD5("20210521111439")
            val hash2 = MD5(hash1 + "d4y1IvMPk6jbeC6p9aG6G5FIV0YR7ypT")
            Log.e("timestamp11", hash1)
            Log.e("timestamp11", hash2)
        } catch (e: Exception) {
            Log.e("MD5 Error", "Failed to compute MD5 hash", e)
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()

        BleManager.getInstance().destroy()
        Log.e("onDestroy", "onDestroy")
    }

    fun setBle() {
        if (BleManager.getInstance().isSupportBle()) {
            Log.e("setble", "支持蓝牙")
            if (BleManager.getInstance().isBlueEnable()) {
                Log.e("setble", "蓝牙可用")
            } else {
                BleManager.getInstance().enableBluetooth()
            }
        } else {
        }
    }

    private val requestSinglePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied! The app may not function properly.", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions denied! The app may not function properly.", Toast.LENGTH_SHORT).show()
            }
        }


    fun checkAndRequestPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            // All permissions granted
            Toast.makeText(this, "All permissions are already granted", Toast.LENGTH_SHORT).show()
        } else {
            // Request missing permissions
            requestMultiplePermissionsLauncher.launch(missingPermissions.toTypedArray())
        }
    }


    private fun onPermissionsGranted() {
        Toast.makeText(this, "Permissions granted! Ready to proceed.", Toast.LENGTH_SHORT).show()
        // Proceed with functionality that requires permissions
    }

    private val firstBollen = false

    fun initListener() {
        // Request permissions launcher using ActivityResultContracts
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.e("Permissions", "Permission granted")
            } else {
                Log.e("Permissions", "Permission denied")
                Toast.makeText(this, getString(R.string.permission_denied_message), Toast.LENGTH_SHORT).show()
            }
        }

        // Handle RadioGroup checked changes
        radioIndex.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.tag_home -> {
                    Log.e("onCheckedChanged", "tag_home")
                    viewPager.currentItem = 0
                }

                R.id.msg_home2 -> {
                    if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.e("onCheckedChanged", "msg_home2")
                        viewPager.currentItem = 1
                    } else {
                        Log.e("onCheckedChanged", "Requesting permission for msg_home2")
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }

                R.id.msg_home1 -> {
                    Log.e("onCheckedChanged", "msg_home1")
                    if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        viewPager.currentItem = 2
                    } else {
                        Log.e("onCheckedChanged", "Requesting permission for msg_home1")
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }

                R.id.home_me -> {
                    Log.e("onCheckedChanged", "home_me")
                    if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        viewPager.currentItem = 3
                    } else {
                        Log.e("onCheckedChanged", "Requesting permission for home_me")
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            }
        }
        // Handle ViewPager2 page changes
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> radioIndex.check(R.id.tag_home)
                    1 -> radioIndex.check(R.id.msg_home2)
                    2 -> radioIndex.check(R.id.msg_home1)
                    3 -> radioIndex.check(R.id.home_me)
                }
            }
        })
    }
    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }



    var UUID_KEY_DATA: String = "00002a00-0000-1000-8000-00805f9b34fb"
    var UUID_CHAR1: String = "0000ffe2-0000-1000-8000-00805f9b34fb"
    var UUID_CHAR2: String = "0000ffe1-0000-1000-8000-00805f9b34fb"
    var UUID_CHAR3: String = "0000ae03-0000-1000-8000-00805f9b34fb"
    var UUID_CHAR4: String = "0000ae04-0000-1000-8000-00805f9b34fb"
    var UUID_CHAR5: String = "0000ae05-0000-1000-8000-00805f9b34fb"
    var UUID_CHAR6: String = "0000ae10-0000-1000-8000-00805f9b34fb"
    var UUID_HERATRATE: String = "0000ae3b-0000-1000-8000-00805f9b34fb"
    var UUID_TEMPERATURE: String = "0000ae3c-0000-1000-8000-00805f9b34fb"
    private var isConnect = false

    fun link(bleDevice: BleDevice?) {
        BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {
                Log.e("setble88:", "开始连接")
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                Log.e("setble88:", "连接失败")
                isConnect = false
            }


            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                Log.e("setble88:", "连接成功")
                Log.e("setble88:", "状态码:$status")

                // Save connection state and device MAC in Preferences
                PreferencesUtil.putBoolean(this@BleActiity, "auto", true)
                PreferencesUtil.putString(this@BleActiity, "mac", bleDevice.getMac())

                // Post a link success event
                val messageEvent2 = MessageEvent<BleDevice>().apply {
                    name = mybleDevice?.getName() ?: mybleDevice?.getMac() ?: "Unknown Device"
                    id = EventBusId.linkSuccess
                }
                EventBus.getDefault().post(messageEvent2)

                // Post an update time event
                val messageEvent1 = MessageEvent<BleDevice>().apply {
                    id = EventBusId.upDataTime
                }
                EventBus.getDefault().post(messageEvent1)

                // Mark as connected and perform additional actions
                isConnect = true
                commet()
            }


            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                bleDevice: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                Log.e("setble8811:", "状态码:$status")
                Log.e("setble8811:", "状态码:" + bleDevice.toString())
                if (status == 8) {
                    Toast.makeText(this@BleActiity, "已断开连接", Toast.LENGTH_SHORT).show()
                    BleManager.getInstance().disconnect(mybleDevice)
                    val messageEvent: MessageEvent<BleDevice> = MessageEvent<BleDevice>()
                    messageEvent.id = EventBusId.UODATE1
                    EventBus.getDefault().post(messageEvent)
                    mybleDevice = null
                }
            }
        })
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun commet() {
        val gatt: BluetoothGatt = BleManager.getInstance().getBluetoothGatt(mybleDevice)
        val serviceList: List<BluetoothGattService> = gatt.getServices()
        for (service in serviceList) {
            val uuid_service: UUID = service.getUuid()
            val characteristicList: List<BluetoothGattCharacteristic> = service.getCharacteristics()
            for (characteristic in characteristicList) {
                val uuid_chara: UUID = characteristic.getUuid()
                Log.e("setble88664411", uuid_chara.toString())

                if (uuid_chara.toString() == UUID_CHAR2) {
                    characteristics[1] = characteristic
                    list.add(characteristic)
                    ble_connect(mybleDevice, 2)
                }
                if (uuid_chara.toString() == UUID_CHAR1) {
                    characteristics[0] = characteristic
                    list.add(characteristic)
                }

                //                Log.e("characteristic",uuid_chara.);
                val charaProp: Int = characteristic.getProperties()
            }
        }
    }

    var list: ArrayList<BluetoothGattCharacteristic> = ArrayList<BluetoothGattCharacteristic>()
    var mybleDevice: BleDevice? = null
    private val characteristics = MutableList<BluetoothGattCharacteristic?>(9) { null }
    private var isConfing = false
    private val isStop = false


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun ble_connect(bleDevice: BleDevice?, type: Int) {
        Log.e("setble88999", "ble_connect")

        // Ensure characteristic2 exists
        val characteristic2 = characteristics[1] // Second characteristic
        if (type == 2 && characteristic2 != null) {
            setupNotification(bleDevice, characteristic2)
        }

        setupMTU(bleDevice)
    }

    private fun setupNotification(bleDevice: BleDevice?, characteristic: BluetoothGattCharacteristic) {
        BleManager.getInstance().notify(
            bleDevice,
            characteristic.service.uuid.toString(),
            characteristic.uuid.toString(),
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    Log.e("setble66633", "onNotifySuccess2")
                    postEvent(EventBusId.UODATE, mybleDevice)
                }

                override fun onNotifyFailure(exception: BleException) {
                    Log.e("setble666633", "onNotifyFailure2")
                }

                override fun onCharacteristicChanged(data: ByteArray) {
                    handleCharacteristicChanged(data)
                }
            }
        )
    }

    private fun handleCharacteristicChanged(data: ByteArray) {
        val hexData = HexUtil.formatHexString(data, false)
        Log.e("setble666332", hexData)

        if (TextUtils.isStart(hexData)) {
            isConfing = true
        }

        if (TextUtils.isStop(hexData)) {
            isConfing = false
            postEvent(EventBusId.upConfig, hexData)
        }

        if (isConfing) {
            postEvent(EventBusId.upConfig, hexData)
        }

        when {
            TextUtils.passwordInput(hexData) -> showToast(R.string.passwordInput)
            TextUtils.passwordErr(hexData) -> showToast(R.string.passwordErr)
            TextUtils.passwordSuccess(hexData) -> showToast(R.string.passwordSuccess)
            TextUtils.passwordOnSuccess(hexData) -> showToast(R.string.passwordOnSuccess)
        }

        postEvent(EventBusId.upData, HexUtil.formatHexString(data, true))
    }

    private fun setupMTU(bleDevice: BleDevice?) {
        BleManager.getInstance().setMtu(bleDevice, 512, object : BleMtuChangedCallback() {
            override fun onSetMTUFailure(exception: BleException) {
                Log.e("setbleononReadFailure", "onSetMTUFailure")
            }

            override fun onMtuChanged(mtu: Int) {
                Log.e("setbleononReadFailure", "onMtuChanged:$mtu")
            }
        })
    }

    private fun postEvent(id: String, body: Any? = null) {
        val messageEvent = MessageEvent<Any>().apply {
            this.id = id
            this.body = body
        }
        EventBus.getDefault().post(messageEvent)
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun send(event: MessageEvent<String?>) {
        if (event.id != EventBusId.send) {
            return
        }
        //        MessageEvent<String> messageEvent = new MessageEvent<>();
//        messageEvent.setId(EventBusId.dissLoading);
//        EventBus.getDefault().post(messageEvent);
//        loadingPopup.dismiss();
        send(event.body!!)
    }

    fun send(hex: String) {
        val bleDevice = mybleDevice
        val characteristic = characteristics.getOrNull(1)

        // Check for null values
        if (bleDevice == null) {
            showToast(R.string.placeBle)
            return
        }

        if (characteristic == null) {
            Log.e("BleSend", "Characteristic is null, cannot send data.")
            return
        }

        // Convert hex string and append "0A"
        val hexData = TextUtils.strToASCII(hex) + "0A"
        Log.e("BleSend", "Hex Data: $hexData")

        // Write to BLE device
        BleManager.getInstance().write(
            bleDevice,
            characteristic.service.uuid.toString(),
            characteristic.uuid.toString(),
            HexUtil.hexStringToBytes(hexData),
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
                    Log.e("BleSend", "Data written successfully: ${HexUtil.formatHexString(justWrite, true)}")
                }

                override fun onWriteFailure(exception: BleException) {
                    Log.e("BleSend", "Failed to write data: $exception")
                }
            }
        )
    }

    companion object {
        @Throws(Exception::class)
        fun MD5(data: String): String {
            println(data)
            val md = MessageDigest.getInstance("MD5")
            val array = md.digest(data.toByteArray(charset("UTF-8")))
            val sb = StringBuilder()
            for (item in array) {
                sb.append(Integer.toHexString((item.toInt() and 0xFF) or 0x100).substring(1, 3))
            }
            println(sb.toString().uppercase(Locale.getDefault()))
            return sb.toString().uppercase(Locale.getDefault())
        }
    }
}
