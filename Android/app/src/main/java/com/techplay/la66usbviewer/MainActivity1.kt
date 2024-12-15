package com.techplay.la66usbviewer

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleIndicateCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.clj.fastble.utils.HexUtil

class MainActivity1 : AppCompatActivity() {
    private lateinit var listView: RecyclerView
    private lateinit var mResultAdapter: ResultAdapter
    private lateinit var editText: EditText
    private lateinit var txtble: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        BleManager.getInstance().init(application)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .setOperateTimeout(5000)
        listView = findViewById(R.id.recyclerView)
        listView.layoutManager = LinearLayoutManager(this) // Vertical list
        mResultAdapter = ResultAdapter(this)
        listView.adapter = mResultAdapter
        editText = findViewById(R.id.editText)
        txtble = findViewById(R.id.txtble)
        findViewById<View>(R.id.btn1).setOnClickListener { setble() }
        findViewById<View>(R.id.btn2).setOnClickListener(View.OnClickListener {
            if (mybleDevice == null) {
                Toast.makeText(
                    this@MainActivity1,
                    "请搜索蓝牙设备",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }
            link(mybleDevice)
        })
        findViewById<View>(R.id.btn3).setOnClickListener { send() }

        //        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//            @Override
//            public void onClick(View v) {
//                write();
//            }
//        });
        findViewById<View>(R.id.btn5).setOnClickListener { send3() }
        findViewById<View>(R.id.btn6).setOnClickListener { send6() }
        findViewById<View>(R.id.btn7).setOnClickListener { send9() }
        findViewById<View>(R.id.btn8).setOnClickListener {
            if (editText.getText().toString().trim { it <= ' ' }.length == 0) {
                Toast.makeText(
                    this@MainActivity1,
                    resources.getText(R.string.instructions),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                send7(editText.getText().toString().trim { it <= ' ' })
            }
        }
        chechLocation()
    }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted
                onLocationPermissionGranted()
            } else {
                // Permission denied
                showPermissionDeniedToast()
            }
        }

    fun chechLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            onLocationPermissionGranted()
        } else {
            // Request permission
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun onLocationPermissionGranted() {
        // Perform actions that require location permissions
        Log.d("Permission", "Location permission granted.")
    }

    private fun showPermissionDeniedToast() {
        Toast.makeText(this, getString(R.string.permission_denied_message), Toast.LENGTH_SHORT).show()
    }


    //    @Override
    //    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    //        // 一些权限被授予
    //        Toast.makeText(this, "允许", Toast.LENGTH_SHORT).show();
    //    }
    //
    //    @Override
    //    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    //        // 一些权限被禁止
    //        Toast.makeText(this, "禁止", Toast.LENGTH_SHORT).show();
    //    }
    //
    //    @Override
    //    public void onPermissionsDenied(int requestCode, List<String> perms) {
    //        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
    //            new AppSettingsDialog.Builder(this).build().show();
    //            //弹出个对话框 可以自定义
    //        }
    //    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun setBle() {
        if (BleManager.getInstance().isSupportBle) {
            Log.e("setble", "支持蓝牙")
            if (BleManager.getInstance().isBlueEnable) {
                Log.e("setble", "蓝牙可用")
            } else {
                BleManager.getInstance().enableBluetooth()
            }
        } else {
        }
    }

    fun setble() {
//               .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
//                .setDeviceName(true, names)         // 只扫描指定广播名的设备，可选
//                .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
//                .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
        Log.e("setble", "setble")
        val scanRuleConfig = BleScanRuleConfig.Builder()

            .setScanTimeOut(10000) // 扫描超时时间，可选，默认10秒
            .build()
        BleManager.getInstance().initScanRule(scanRuleConfig)

        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
            }

            override fun onLeScan(bleDevice: BleDevice) {
                Log.e("setble33", bleDevice.mac)
            }

            override fun onScanning(bleDevice: BleDevice) {
                Log.e("setble22", bleDevice.mac)
            }

            override fun onScanFinished(scanResultList: List<BleDevice>) {
                for (d in scanResultList) {
                    Log.e("setble99:", d.mac)
                    if (d.name != null) {
                        Log.e("setble99Name:", d.name)
                        mResultAdapter.addResult("发现蓝牙设备:" + d.name)
                        mResultAdapter.notifyDataSetChanged()
                        if (d.name.equals("AC696X_1(BLE)", ignoreCase = true)) {
                            txtble.text = "发现蓝牙设备"
                            mybleDevice = d
                        }
                    } else {
                        mResultAdapter.addResult("发现蓝牙设备:" + d.mac)
                    }
                }
            }
        })
    }

    var UUID_KEY_DATA: String = "00002a00-0000-1000-8000-00805f9b34fb"
    var UUID_CHAR1: String = "0000ae01-0000-1000-8000-00805f9b34fb"
    var UUID_CHAR2: String = "0000ae02-0000-1000-8000-00805f9b34fb"
    var UUID_CHAR3: String = "0000ae03-0000-1000-8000-00805f9b34fb"
    var UUID_CHAR4: String = "0000ae04-0000-1000-8000-00805f9b34fb"
    var UUID_CHAR5: String = "0000ae05-0000-1000-8000-00805f9b34fb"
    var UUID_CHAR6: String = "0000ae10-0000-1000-8000-00805f9b34fb"
    var UUID_HERATRATE: String = "0000ae3b-0000-1000-8000-00805f9b34fb"
    var UUID_TEMPERATURE: String = "0000ae3c-0000-1000-8000-00805f9b34fb"

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun link(bleDevice: BleDevice?) {
        mResultAdapter.clear()
        mResultAdapter.notifyDataSetChanged()
        BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {
                Log.e("setble88:", "开始连接")
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                Log.e("setble88:", "连接失败")
            }


            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                Log.e("setble88:", "连接成功")
                Log.e("setble88:", "状态码:$status")
                txtble.text = "蓝牙连接成功"
                commet()
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                bleDevice: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                Log.e("setble8811:", "状态码:$status")
                Log.e("setble8811:", "状态码:$bleDevice")
            }
        })
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun commet() {
        val gatt = BleManager.getInstance().getBluetoothGatt(mybleDevice)
        val serviceList = gatt.services
        for (service in serviceList) {
            val uuid_service = service.uuid
            val characteristicList = service.characteristics
            for (characteristic in characteristicList) {
                val uuid_chara = characteristic.uuid
                Log.e("setble88664411", uuid_chara.toString())
                if (uuid_chara.toString() == UUID_CHAR1) {
                    characteristic1 = characteristic
                    list.add(characteristic)
                    ble_connect(mybleDevice, 1)
                }
                if (uuid_chara.toString() == UUID_CHAR2) {
                    characteristic2 = characteristic
                    list.add(characteristic)
                    ble_connect(mybleDevice, 2)
                }

                if (uuid_chara.toString() == UUID_CHAR3) {
                    characteristic3 = characteristic
                    list.add(characteristic)
                    ble_connect(mybleDevice, 3)
                }

                if (uuid_chara.toString() == UUID_CHAR4) {
                    characteristic4 = characteristic
                    list.add(characteristic)
                    ble_connect(mybleDevice, 4)
                }

                if (uuid_chara.toString() == UUID_CHAR5) {
                    characteristic5 = characteristic
                    list.add(characteristic)
                    ble_connect(mybleDevice, 5)
                }

                if (uuid_chara.toString() == UUID_CHAR6) {
                    characteristic6 = characteristic
                    list.add(characteristic)
                    ble_connect(mybleDevice, 6)
                }

                if (uuid_chara.toString() == UUID_HERATRATE) {
                    characteristic7 = characteristic
                    list.add(characteristic)
                    ble_connect(mybleDevice, 7)
                }
                if (uuid_chara.toString() == UUID_TEMPERATURE) {
                    characteristic8 = characteristic
                    list.add(characteristic)
                    ble_connect(mybleDevice, 8)
                }
                if (uuid_chara.toString() == UUID_KEY_DATA) {
                    characteristic9 = characteristic
                    list.add(characteristic)
                    ble_connect(mybleDevice, 8)
                }
                //                Log.e("characteristic",uuid_chara.);
                val charaProp = characteristic.properties
            }
        }
    }

    var list: ArrayList<BluetoothGattCharacteristic> = ArrayList()
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun ble_connect(bleDevice: BleDevice?, type: Int) {
        Log.e("setble88999", "ble_connect")
        if (type == 2) BleManager.getInstance().notify(
            bleDevice,
            characteristic2!!.service.uuid.toString(),
            characteristic2!!.uuid.toString(),
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    // 打开通知操作成功
                    Log.e("setble66633", "onNotifySuccess2")
                }

                override fun onNotifyFailure(exception: BleException) {
                    Log.e("setble666633", "onNotifyFailure2")
                    // 打开通知操作失败
                }

                override fun onCharacteristicChanged(data: ByteArray) {
                    // 打开通知后，设备发过来的数据将在这里出现
                    Log.e("setble666332", HexUtil.formatHexString(data, true))

                    add("已接收" + HexUtil.formatHexString(data, true))
                }
            })
        if (type == 4) BleManager.getInstance().notify(
            bleDevice,
            characteristic4!!.service.uuid.toString(),
            characteristic4!!.uuid.toString(),
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    // 打开通知操作成功
                    Log.e("setble66633", "onNotifySuccess4")
                }

                override fun onNotifyFailure(exception: BleException) {
                    Log.e("setble666633", "onNotifyFailure4")
                    // 打开通知操作失败
                }

                override fun onCharacteristicChanged(data: ByteArray) {
                    // 打开通知后，设备发过来的数据将在这里出现
                    Log.e("setble666334", HexUtil.formatHexString(data, true))
                }
            })

        if (type == 5) BleManager.getInstance().indicate(
            bleDevice,
            characteristic5!!.service.uuid.toString(),
            characteristic5!!.uuid.toString(),
            object : BleIndicateCallback() {
                override fun onIndicateSuccess() {
                    Log.e("setble66333333", "onIndicateSuccess5")
                    // 打开通知操作成功
                }

                override fun onIndicateFailure(exception: BleException) {
                    // 打开通知操作失败
                    Log.e("setble66333333", "exception5")
                }

                override fun onCharacteristicChanged(data: ByteArray) {
                    // 打开通知后，设备发过来的数据将在这里出现
                    Log.e("setble663333335", HexUtil.formatHexString(data, true))
                }
            })
        try {
            if (type == 8) BleManager.getInstance().notify(
                bleDevice,
                characteristic8!!.service.uuid.toString(),
                characteristic8!!.uuid.toString(),
                object : BleNotifyCallback() {
                    override fun onNotifySuccess() {
                        // 打开通知操作成功
                        Log.e("setble66633", "onNotifySuccess8")
                    }

                    override fun onNotifyFailure(exception: BleException) {
                        Log.e("setble666633", "onNotifyFailure8")
                        // 打开通知操作失败
                    }

                    override fun onCharacteristicChanged(data: ByteArray) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.e("setble666338", HexUtil.formatHexString(data, true))
                    }
                })
        } catch (e: Exception) {
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun send() {
        if (mybleDevice == null) {
            Toast.makeText(
                this@MainActivity1,
                resources.getString(R.string.placeBle),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val hex = "5A810100000000000000000000000001"
        BleManager.getInstance().write(
            mybleDevice,
            characteristic1!!.service.uuid.toString(),
            characteristic1!!.uuid.toString(),
            HexUtil.hexStringToBytes(hex),
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
                    // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                    Log.e("setblesend", HexUtil.formatHexString(justWrite, true))
                    add("已发送" + HexUtil.formatHexString(justWrite, true))
                }

                override fun onWriteFailure(exception: BleException) {
                    Log.e("setbleononReadFailure", "onWriteFailure")
                    // 发送数据到设备失败
                }
            })
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun send9() {
        if (mybleDevice == null) {
            Toast.makeText(
                this@MainActivity1,
                resources.getString(R.string.placeBle),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val hex = "5A830100000000000000000000000001"
        BleManager.getInstance().write(
            mybleDevice,
            characteristic1!!.service.uuid.toString(),
            characteristic1!!.uuid.toString(),
            HexUtil.hexStringToBytes(hex),
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
                    // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                    Log.e("setblesend", HexUtil.formatHexString(justWrite, true))

                    add("已发送" + HexUtil.formatHexString(justWrite, true))
                }

                override fun onWriteFailure(exception: BleException) {
                    Log.e("setbleononReadFailure", "onWriteFailure")
                    // 发送数据到设备失败
                }
            })
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun send3() {
        if (mybleDevice == null) {
            Toast.makeText(
                this@MainActivity1,
                resources.getString(R.string.placeBle),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val hex = "5A810000000000000000000000000001"

        BleManager.getInstance().write(
            mybleDevice,
            characteristic1!!.service.uuid.toString(),
            characteristic1!!.uuid.toString(),
            HexUtil.hexStringToBytes(hex),
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
                    // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                    Log.e("setblesend", HexUtil.formatHexString(justWrite, true))
                    add("已发送" + HexUtil.formatHexString(justWrite, true))
                }

                override fun onWriteFailure(exception: BleException) {
                    Log.e("setbleononReadFailure", "onWriteFailure")
                    // 发送数据到设备失败
                }
            })
    }

    private fun add(aa: String) {
        mResultAdapter.addResult(aa)
        mResultAdapter.notifyDataSetChanged()
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun send6() {
        if (mybleDevice == null) {
            Toast.makeText(
                this@MainActivity1,
                resources.getString(R.string.placeBle),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val hex = "5A820100000000000000000000000001"

        BleManager.getInstance().write(
            mybleDevice,
            characteristic1!!.service.uuid.toString(),
            characteristic1!!.uuid.toString(),
            HexUtil.hexStringToBytes(hex),
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
                    // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                    Log.e("setblesend", HexUtil.formatHexString(justWrite, true))
                    add("已发送" + HexUtil.formatHexString(justWrite, true))
                }

                override fun onWriteFailure(exception: BleException) {
                    Log.e("setbleononReadFailure", "onWriteFailure")
                    // 发送数据到设备失败
                }
            })
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun send7(hex: String?) {
        if (mybleDevice == null) {
            Toast.makeText(
                this@MainActivity1,
                resources.getString(R.string.placeBle),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        BleManager.getInstance().write(
            mybleDevice,
            characteristic1!!.service.uuid.toString(),
            characteristic1!!.uuid.toString(),
            HexUtil.hexStringToBytes(hex),
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
                    // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                    Log.e("setblesend", HexUtil.formatHexString(justWrite, true))
                    add("已发送" + HexUtil.formatHexString(justWrite, true))
                }

                override fun onWriteFailure(exception: BleException) {
                    Log.e("setbleononReadFailure", "onWriteFailure")
                    // 发送数据到设备失败
                }
            })
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun write() {
        for (i in list.indices) BleManager.getInstance().read(
            mybleDevice,
            list[i].service.uuid.toString(),
            list[i].uuid.toString(),
            object : BleReadCallback() {
                override fun onReadSuccess(data: ByteArray) {
                    // 读特征值数据成功
                    Log.e("setbleonReadSuccess", HexUtil.formatHexString(data, true))
                }

                override fun onReadFailure(exception: BleException) {
                    // 读特征值数据失败
                    Log.e("setbleononReadFailure", "455454")
                }
            })
    }


    class ResultAdapter(
        private val context: Context,
        private val characteristicList: MutableList<String> = ArrayList()
    ) : RecyclerView.Adapter<ResultAdapter.ViewHolder>() {

        // ViewHolder to hold the views for each item
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtTitle: TextView = itemView.findViewById(R.id.txt_title)
            val txtUuid: TextView = itemView.findViewById(R.id.txt_uuid)
            val txtType: TextView = itemView.findViewById(R.id.txt_type)
        }

        // Add a new result to the list
        fun addResult(service: String) {
            characteristicList.add(service)
            notifyItemInserted(characteristicList.size - 1)
        }

        // Clear the list
        fun clear() {
            characteristicList.clear()
            notifyDataSetChanged()
        }

        // Inflate the item layout
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.adapter_service, parent, false)
            return ViewHolder(view)
        }

        // Bind data to the views
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.txtTitle.text = "数据:"
            holder.txtUuid.text = characteristicList[position]
            holder.txtType.text = "" // Set additional data if needed
        }

        // Return the size of the list
        override fun getItemCount(): Int = characteristicList.size
    }

}