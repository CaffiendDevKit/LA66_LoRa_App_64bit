package com.techplay.la66usbviewer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.BarUtils
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.clj.fastble.utils.HexUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.techplay.la66usbviewer.bean.MessageEvent
import com.techplay.la66usbviewer.config.EventBusId
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

        chechLocation()
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

            // Navigate to SearchActivity
            Intent(this, SearchActivity::class.java).apply {
                putExtra("mac", mac)
                putExtra("name", name)
                startActivity(this)
            }

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

    fun getDevice(): BleDevice? {
        return device
    }

    fun setDevice(device: BleDevice?) {
        this.device = device
    }

    fun getMybleDevice(): BleDevice? {
        return mybleDevice
    }

    fun setMybleDevice(mybleDevice: BleDevice?) {
        this.mybleDevice = mybleDevice
    }

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
        if (device.getMac() == mybleDevice.getMac()) {
            Toast.makeText(this@BleActiity, "已连接该设备", Toast.LENGTH_SHORT).show()
        } else {
            val messageEvent1: MessageEvent<BleDevice> = MessageEvent<BleDevice>()
            messageEvent1.id = EventBusId.onreLink
            EventBus.getDefault().post(messageEvent1)
            XPopup.Builder(this@BleActiity)
                .asConfirm("发现新设备", "是否从新连接到新设备", object : OnConfirmListener {
                    override fun onConfirm() {
                        BleManager.getInstance().disconnect(mybleDevice)
                        mybleDevice = device
                        link(mybleDevice)
                    }
                }).show()
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
        if (event.id != EventBusId.goSearch) {
            return
        }

        Log.e("PreferencesUtil", "PreferencesUtil1")
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
    }

    private var viewPager: NoScrollViewPager? = null
    private var radioIndex: RadioGroup? = null

    private var fragmentList: MutableList<Fragment>? = null
    private var indexFragmentPageAdapter: IndexFragmentPageAdapter? = null

    fun initView() {
        viewPager = findViewById<NoScrollViewPager>(R.id.view_pager)
        radioIndex = findViewById<RadioGroup>(R.id.radio_index)
        fragmentList = ArrayList()
        fragmentList.add(HomeFragment())
        fragmentList.add(DeviceInfoFragment())
        fragmentList.add(ConfigFragment())
        fragmentList.add(LogFragment())
        val fm: FragmentManager = getSupportFragmentManager()
        indexFragmentPageAdapter = IndexFragmentPageAdapter(fm, fragmentList)
        viewPager.setNoScroll(true)
        viewPager.setAdapter(indexFragmentPageAdapter)
        viewPager.setOffscreenPageLimit(3)
        viewPager.setCurrentItem(0)
        initListener()
        val df: DateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        val timestamp = df.format(Date())
        Log.e("timestamp", timestamp)
        try {
            Log.e("timestamp11", MD5("20210521111439"))
            Log.e("timestamp11", MD5(MD5("20210521111439") + "d4y1IvMPk6jbeC6p9aG6G5FIV0YR7ypT"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setBle()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            //从设置页面返回，判断权限是否申请。
            val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (EasyPermissions.hasPermissions(this, perms)) {
            } else {
                Toast.makeText(this, "权限申请失败!将无法正常使用APP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun chechLocation() {
        val perms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (EasyPermissions.hasPermissions(this, perms)) {
            // 已获取权限
            // ...
        } else {
            // 没有权限，现在去获取
            // ...
            EasyPermissions.requestPermissions(
                this, getResources().getText(R.string.applyBlue).toString(),
                10001, perms
            )
        }
    }

    @AfterPermissionGranted(10001)
    fun onPermissionSuccess() {
        Toast.makeText(this, "AfterPermission调用成功了", Toast.LENGTH_SHORT).show()
    }

    @AfterPermissionGranted(10002)
    fun onPermissionSuccessWrite() {
    }

    private val firstBollen = false

    fun initListener() {
        radioIndex.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
                val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                when (checkedId) {
                    R.id.tag_home -> {
                        Log.e("onCheckedChanged", "tag_home")
                        viewPager.setCurrentItem(0)
                    }

                    R.id.msg_home2 -> if (EasyPermissions.hasPermissions(this@BleActiity, perms)) {
                        // 已获取权限
                        // ...
//                            if (!firstBollen) {
//                                firstBollen = true;
//                                MessageEvent<String> messageEvent = new MessageEvent<>();
//                                messageEvent.setId(EventBusId.START);
//                                EventBus.getDefault().post(messageEvent);
//                            }
                        Log.e("onCheckedChanged", "msg_home2")
                        //                            MessageEvent<String> messageEvent = new MessageEvent<>();
//                            messageEvent.setId(EventBusId.deviceDetails);
//                            EventBus.getDefault().post(messageEvent);
                        viewPager.setCurrentItem(1)
                        return
                    } else {
                        // 没有权限，现在去获取
                        // ...
                        EasyPermissions.requestPermissions(
                            this@BleActiity,
                            getResources().getText(R.string.applyBlue).toString(),
                            10001,
                            perms
                        )
                    }

                    R.id.msg_home1 -> {
                        Log.e("onCheckedChanged", "msg_home1")
                        if (EasyPermissions.hasPermissions(this@BleActiity, perms)) {
                            // 已获取权限
                            // ...
//                            if (!firstBollen) {
//                                firstBollen = true;
//                                MessageEvent<String> messageEvent = new MessageEvent<>();
//                                messageEvent.setId(EventBusId.START);
//                                EventBus.getDefault().post(messageEvent);
//                            }
                            viewPager.setCurrentItem(2)
                        } else {
                            // 没有权限，现在去获取
                            // ...
                            EasyPermissions.requestPermissions(
                                this@BleActiity,
                                getResources().getText(R.string.applyBlue).toString(),
                                10001,
                                perms
                            )
                        }
                    }

                    R.id.home_me -> {
                        Log.e("onCheckedChanged", "home_me")
                        if (EasyPermissions.hasPermissions(this@BleActiity, perms)) {
                            // 已获取权限
                            // ...
                            viewPager.setCurrentItem(3)
                        } else {
                            // 没有权限，现在去获取
                            // ...
                            EasyPermissions.requestPermissions(
                                this@BleActiity,
                                getResources().getText(R.string.applyBlue).toString(),
                                10001,
                                perms
                            )
                        }
                    }
                }
            }
        })
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {
            }

            override fun onPageSelected(i: Int) {
                when (i) {
                    0 -> radioIndex.check(R.id.tag_home)
                    1 -> radioIndex.check(R.id.msg_home2)
                    2 -> radioIndex.check(R.id.msg_home1)
                    3 -> radioIndex.check(R.id.home_me)
                }
            }

            override fun onPageScrollStateChanged(i: Int) {
            }
        })
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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

                PreferencesUtil.putBoolean(this@BleActiity, "auto", true)
                PreferencesUtil.putString(this@BleActiity, "mac", bleDevice.getMac())
                val messageEvent2: MessageEvent<BleDevice> = MessageEvent<BleDevice>()

                messageEvent2.name =
                    if (mybleDevice.getName() != null) mybleDevice.getName() else mybleDevice.getMac()
                messageEvent2.id = EventBusId.linkSuccess
                EventBus.getDefault().post(messageEvent2)
                //                txtble.setText("蓝牙连接成功");
                val messageEvent1: MessageEvent<BleDevice> = MessageEvent<BleDevice>()
                messageEvent1.id = EventBusId.upDataTime
                EventBus.getDefault().post(messageEvent1)
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
                    characteristic2 = characteristic
                    list.add(characteristic)
                    ble_connect(mybleDevice, 2)
                }
                if (uuid_chara.toString() == UUID_CHAR1) {
                    characteristic1 = characteristic
                    list.add(characteristic)
                }

                //                Log.e("characteristic",uuid_chara.);
                val charaProp: Int = characteristic.getProperties()
            }
        }
    }

    var list: ArrayList<BluetoothGattCharacteristic> = ArrayList<BluetoothGattCharacteristic>()
    var characteristic1: BluetoothGattCharacteristic? = null
    var characteristic2: BluetoothGattCharacteristic? = null
    var characteristic3: BluetoothGattCharacteristic? = null
    var characteristic4: BluetoothGattCharacteristic? = null
    var characteristic5: BluetoothGattCharacteristic? = null
    var characteristic6: BluetoothGattCharacteristic? = null

    var characteristic7: BluetoothGattCharacteristic? = null
    var characteristic8: BluetoothGattCharacteristic? = null
    var characteristic9: BluetoothGattCharacteristic? = null
    var mybleDevice: BleDevice? = null
    private var isConfing = false
    private val isStop = false

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun ble_connect(bleDevice: BleDevice?, type: Int) {
        Log.e("setble88999", "ble_connect")
        if (type == 2) BleManager.getInstance().notify(
            bleDevice,
            characteristic2.getService().getUuid().toString(),
            characteristic2.getUuid().toString(),
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    // 打开通知操作成功
                    Log.e("setble66633", "onNotifySuccess2")
                    val messageEvent: MessageEvent<BleDevice> = MessageEvent<BleDevice>()
                    messageEvent.body = mybleDevice
                    messageEvent.id = EventBusId.UODATE
                    EventBus.getDefault().post(messageEvent)
                }

                override fun onNotifyFailure(exception: BleException) {
                    Log.e("setble666633", "onNotifyFailure2")
                    // 打开通知操作失败
                }

                override fun onCharacteristicChanged(data: ByteArray) {
                    // 打开通知后，设备发过来的数据将在这里出现
                    Log.e("setble666332", HexUtil.formatHexString(data, false))

                    Log.e(
                        "2upConfig",
                        TextUtils.isStart(HexUtil.formatHexString(data, false)).toString() + ""
                    )
                    Log.e(
                        "1upConfig",
                        TextUtils.isStop(HexUtil.formatHexString(data, false)).toString() + ""
                    )
                    if (TextUtils.isStart(HexUtil.formatHexString(data, false))) {
                        isConfing = TextUtils.isStart(HexUtil.formatHexString(data, false))
                    }
                    if (TextUtils.isStop(HexUtil.formatHexString(data, false))) {
                        isConfing = TextUtils.isStart(HexUtil.formatHexString(data, false))
                        val messageEvent2 = MessageEvent<String>()
                        messageEvent2.body = HexUtil.formatHexString(data, false)
                        messageEvent2.id = EventBusId.upConfig
                        EventBus.getDefault().post(messageEvent2)
                    }
                    if (isConfing) {
                        val messageEvent2 = MessageEvent<String>()
                        messageEvent2.body = HexUtil.formatHexString(data, false)
                        messageEvent2.id = EventBusId.upConfig
                        EventBus.getDefault().post(messageEvent2)
                    }
                    if (TextUtils.passwordInput(HexUtil.formatHexString(data, false))) {
                        Toast.makeText(
                            this@BleActiity,
                            getResources().getString(R.string.passwordInput),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (TextUtils.passwordErr(HexUtil.formatHexString(data, false))) {
                        Toast.makeText(
                            this@BleActiity,
                            getResources().getString(R.string.passwordErr),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (TextUtils.passwordSuccess(HexUtil.formatHexString(data, false))) {
                        Toast.makeText(
                            this@BleActiity,
                            getResources().getString(R.string.passwordSuccess),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (TextUtils.passwordOnSuccess(HexUtil.formatHexString(data, false))) {
                        Toast.makeText(
                            this@BleActiity,
                            getResources().getString(R.string.passwordOnSuccess),
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                    val messageEvent = MessageEvent<String>()
                    messageEvent.body = HexUtil.formatHexString(data, true)
                    messageEvent.id = EventBusId.upData
                    EventBus.getDefault().post(messageEvent)


                    //数据解析
//                            add("已接收" + HexUtil.formatHexString(data, true));
                }
            })

        //        Boolean auto = PreferencesUtil.getBoolean(BleActiity.this, "auto", false);
//        if (auto) {
//            MessageEvent<String> messageEvent = new MessageEvent<>();
//            messageEvent.setId(EventBusId.Default);
//            EventBus.getDefault().post(messageEvent);
//        }
        BleManager.getInstance().setMtu(bleDevice, 512, object : BleMtuChangedCallback() {
            override fun onSetMTUFailure(exception: BleException) {
                Log.e("setbleononReadFailure", "onSetMTUFailure")
            }

            override fun onMtuChanged(mtu: Int) {
                Log.e("setbleononReadFailure", "onMtuChanged:$mtu")
            }
        })
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun send(hex: String) {
        if (mybleDevice == null) {
            Toast.makeText(
                this@BleActiity,
                getResources().getString(R.string.placeBle),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val hex1 = TextUtils.strToASCII(hex) + "0A"

        Log.e("545", hex1)
        //        BleManager.getInstance().write(
//                mybleDevice,
//                characteristic1.getService().getUuid().toString(),
//                characteristic1.getUuid().toString(),
//                HexUtil.hexStringToBytes(hex1),
//                new BleWriteCallback() {
//                                @Override
//                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
//                        Log.e("setblesend", HexUtil.formatHexString(justWrite, true));
//                    }
//
//                    @Override
//                    public void onWriteFailure(BleException exception) {
//                        Log.e("setbleononReadFailure", "onWriteFailure");
//                        // 发送数据到设备失败
//                    }
//                });
        BleManager.getInstance().write(
            mybleDevice,
            characteristic2.getService().getUuid().toString(),
            characteristic2.getUuid().toString(),
            HexUtil.hexStringToBytes(hex1),
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
                    // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                    Log.e("setblesend", HexUtil.formatHexString(justWrite, true))
                }

                override fun onWriteFailure(exception: BleException) {
                    Log.e("setbleononReadFailure", "onWriteFailure")
                    // 发送数据到设备失败
                }
            })
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
