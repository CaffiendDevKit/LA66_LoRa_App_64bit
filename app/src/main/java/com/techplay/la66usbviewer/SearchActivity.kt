package com.techplay.la66usbviewer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ColorUtils
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import com.google.gson.Gson
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.OnConfirmListener
import com.techplay.la66usbviewer.bean.MessageEvent
import com.techplay.la66usbviewer.config.EventBusId
import com.techplay.la66usbviewer.utils.PreferencesUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
// import pub.devrel.easypermissions.EasyPermissions

class SearchActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_MAC = "mac"
        const val EXTRA_NAME = "name"

        fun start(context: Context, mac: String, name: String? = null) {
            val intent = Intent(context, SearchActivity::class.java).apply {
                putExtra(EXTRA_MAC, mac)
                putExtra(EXTRA_NAME, name)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var listView: RecyclerView
    private lateinit var resultAdapter: ResultAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var txt2: TextView
    private lateinit var mac: String
    private lateinit var name: String
    private lateinit var loadingPopup: BasePopupView

    private val auto: Boolean? = null
    private val isLink: Boolean = false




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mac = requireNotNull(intent.getStringExtra(EXTRA_MAC)) {
            "MAC address is required to launch this activity"
        }
        name = requireNotNull(intent.getStringExtra(EXTRA_NAME)) {
            "NAME is required to launch this activity"
        }

        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.transparent))
        setContentView(R.layout.activity_search)
        EventBus.getDefault().register(this)
        chechLocation()
        initView()
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        swipeRefreshLayout?.isRefreshing = false
    }

    fun initView() {
        findViewById<View>(R.id.title_left).setOnClickListener { finish() }
        loadingPopup = XPopup.Builder(this).asLoading()
        txt2 = findViewById(R.id.txt2)
        txt2.setOnClickListener(View.OnClickListener {
            if (isLink) {
                val messageEvent: MessageEvent<BleDevice> =
                    MessageEvent<BleDevice>()
                messageEvent.id = EventBusId.dislink
                EventBus.getDefault().post(messageEvent)
            }
        })
        if (mac!!.length > 0) {
            txt2.setText((if (name!!.length > 0) name else mac) + "   " + resources.getString(R.string.on_connected))
        } else {
            txt2.setText(resources.getString(R.string.no_connected))
        }
        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        listView = findViewById(R.id.recyclerView)
        listView.layoutManager = LinearLayoutManager(this)
        resultAdapter = ResultAdapter(mac)
        listView.setAdapter(resultAdapter)
        swipeRefreshLayout.setOnRefreshListener { //刷新需执行的操作
            setble()
        }
        setble()
    }

    fun initData() {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLink(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.onLink) {
            return
        }
        XPopup.Builder(this@SearchActivity)
            .asConfirm("发现设备", "是否连接该设备", object : OnConfirmListener {
                override fun onConfirm() {
                    val messageEvent1: MessageEvent<BleDevice> = MessageEvent<BleDevice>()
                    messageEvent1.id = EventBusId.onLinkMain
                    EventBus.getDefault().post(messageEvent1)
                }
            }).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onreLink(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.onreLink) {
            return
        }
        XPopup.Builder(this@SearchActivity)
            .asConfirm("发现新设备", "是否从新连接到新设备", object : OnConfirmListener {
                override fun onConfirm() {
                    val messageEvent1: MessageEvent<BleDevice> = MessageEvent<BleDevice>()
                    messageEvent1.id = EventBusId.onreLinkMain
                    EventBus.getDefault().post(messageEvent1)
                }
            }).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun linkSuccess(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.linkSuccess) {
            return
        }
        txt2!!.text = event.name + "   " + resources.getString(R.string.on_connected)
    }

    fun setble() {
        loadingPopup.show()
        Log.e("setble", "setble")
        val scanRuleConfig: BleScanRuleConfig = BleScanRuleConfig.Builder()

            .setScanTimeOut(10000) // 扫描超时时间，可选，默认10秒
            .build()
        BleManager.getInstance().initScanRule(scanRuleConfig)

        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
            }

            override fun onLeScan(bleDevice: BleDevice) {
                Log.e("lkklj", Gson().toJson(bleDevice))
                Log.e("setble33", bleDevice.getMac())
            }

            override fun onScanning(bleDevice: BleDevice) {
                Log.e("setble22", bleDevice.getMac())
            }

            override fun onScanFinished(scanResultList: MutableList<BleDevice>) {
                myscanResultList.clear() // Clear the previous results
                myscanResultList.addAll(scanResultList) // Add new results
                resultAdapter.addResult(myscanResultList)
                resultAdapter.notifyDataSetChanged()

                Log.e("setble22onScanFinished", myscanResultList.size.toString() + "")
                swipeRefreshLayout.isRefreshing = false
                loadingPopup.dismiss()
                val mac: String = PreferencesUtil.getString(this@SearchActivity, "mac", "")

                for (i in myscanResultList.indices) {
                    Log.e("lkklj", Gson().toJson(myscanResultList[i]))
                    if (myscanResultList[i].getName() != null) {
                        if (myscanResultList[i].getMac().equals(mac, ignoreCase = true)) {
                            val bleDevice: BleDevice = myscanResultList[i]
                            Log.e("4564", bleDevice.getMac())
                            val messageEvent1: MessageEvent<BleDevice> = MessageEvent<BleDevice>()
                            messageEvent1.body = bleDevice
                            messageEvent1.id = EventBusId.auto
                            EventBus.getDefault().post(messageEvent1)
                            break
                        }
                    }
                }


                //                for (BleDevice d : scanResultList) {
//                    Log.e("setble99:", d.getMac());
//                    if (d.getName() != null) {
//                        Log.e("setble99Name:", d.getName());
//                        mResultAdapter.addResult("发现蓝牙设备:" + d.getName());
//                        mResultAdapter.notifyDataSetChanged();
//                        if (d.getName().equalsIgnoreCase("AC696X_1(BLE)")) {
//                            txtble.setText("发现蓝牙设备");
//                            mybleDevice = d;
//                        }
//                    } else {
//                        mResultAdapter.addResult("发现蓝牙设备:" + d.getMac());
//                    }
//                }
            }
        })
    }

    var myscanResultList: MutableList<BleDevice> = mutableListOf()

    class ResultAdapter(private val mac: String) :
        RecyclerView.Adapter<ResultAdapter.ViewHolder>() {

        private val characteristicList = mutableListOf<BleDevice>()

        // Add new items to the list
        fun addResult(newCharacteristicList: List<BleDevice>) {
            characteristicList.clear()
            characteristicList.addAll(newCharacteristicList)
            notifyDataSetChanged()
        }

        // Clear all items
        fun clear() {
            characteristicList.clear()
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = characteristicList.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_ble, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val bleDevice = characteristicList[position]
            holder.bind(bleDevice, mac)
        }

        // ViewHolder class to manage individual item views
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val txtTitle: TextView = itemView.findViewById(R.id.txt_title)
            private val txtUuid: TextView = itemView.findViewById(R.id.txt_uuid)
            private val txtType: TextView = itemView.findViewById(R.id.txt_type)

            fun bind(bleDevice: BleDevice, mac: String) {
                txtTitle.text = bleDevice.getName() ?: bleDevice.getMac() ?: "未知"
                txtUuid.text = bleDevice.getMac()
                txtType.setText(
                    if (mac == bleDevice.getMac()) R.string.on_connected else R.string.no_connected
                )

                itemView.setOnClickListener {
                    // Post the event using EventBus
                    val messageEvent = MessageEvent<BleDevice>().apply {
                        body = bleDevice
                        id = EventBusId.BleDevice
                    }
                    EventBus.getDefault().post(messageEvent)
                }
            }
        }
    }
}
