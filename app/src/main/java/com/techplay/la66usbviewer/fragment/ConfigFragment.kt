package com.techplay.la66usbviewer.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.widget.BaseAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.OnSelectListener
import com.clj.fastble.data.BleDevice
import com.techplay.la66usbviewer.R
import com.techplay.la66usbviewer.bean.MessageEvent
import com.techplay.la66usbviewer.config.EventBusId
import com.techplay.la66usbviewer.utils.LogUtil
import com.techplay.la66usbviewer.utils.PreferencesUtil
import com.techplay.la66usbviewer.utils.TextUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Locale


class ConfigFragment : Fragment(), View.OnClickListener {
    private var selectPopup1: BasePopupView? = null
    private var selectPopup2: BasePopupView? = null
    private var selectPopup3: BasePopupView? = null
    private var selectPopup4: BasePopupView? = null
    private var selectPopup5: BasePopupView? = null
    private var selectPopup6: BasePopupView? = null
    private var auto: Boolean? = null
    private var view1: View? = null
    private var view2: View? = null
    private var btn_send: View? = null
    private var logAdapter: LogAdapter? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    var logList: MutableList<String> = ArrayList()
    private var title: TextView? = null
    private var sendType = 0
    private var timeType = 0
    private var seleteHex = false
    private var sendHex = false

    private var btn_suspend: TextView? = null
    private var txt_time1: TextView? = null
    private var txt_time2: TextView? = null
    private var txt_time3: TextView? = null
    private var txt_time4: TextView? = null
    private var editTextNumber: EditText? = null
    private var editTextTextPersonName: EditText? = null

    private var edittext1: EditText? = null
    private val atList = arrayOf(
        "ATZ",
        "AT+FDR",
        "AT+ADR=0\n",
        "AT+ADR=1\n",
        "AT+NJM=0\n",
        "AT+NJM=1\n",
        "AT+CFM=0\n",
        "AT+CFM=1\n",
        "AT+TDC=1200000\n",
        "AT+MOD=1\n",
        "AT+INTMOD=0\n",
        "AT+INTMOD=1\n",
        "AT+INTMOD=2\n",
        "AT+INTMOD=3\n",
        "AT+5VT=0\n",
        "AT+DR=0\n",
        "AT+TXP=0\n",
        "AT?"
    )
    private lateinit var atDetailsList: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_ble, container, false)
        EventBus.getDefault().register(this)
        initView(view)
        initData()
        Log.e("onCreateView", "onCreateView")

        return view
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        //        if (hidden && !isLink) {
//            Toast.makeText(getActivity(), getResources().getText(R.string.isConnect), Toast.LENGTH_SHORT).show();
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        swipeRefreshLayout?.setRefreshing(false)
    }

    fun initView(view: View) {
        atDetailsList = arrayOf<String>(
            resources.getString(R.string.at1),
            resources.getString(R.string.at2),
            resources.getString(R.string.at3),
            resources.getString(R.string.at3),
            resources.getString(R.string.at4),
            resources.getString(R.string.at4),
            resources.getString(R.string.at5),
            resources.getString(R.string.at5),
            resources.getString(R.string.at6),
            resources.getString(R.string.at7),
            resources.getString(R.string.at8),
            resources.getString(R.string.at8),
            resources.getString(R.string.at8),
            resources.getString(R.string.at8),
            resources.getString(R.string.at9),
            resources.getString(R.string.at10),
            resources.getString(R.string.at11),
            resources.getString(R.string.at12),
        )
        val txt_atdetails = view.findViewById<TextView>(R.id.txt_atdetails)
        val edittext1 = view.findViewById<EditText>(R.id.edittext1)
        val btn_selete = view.findViewById<TextView>(R.id.btn_selete)
        val btn_send_hex = view.findViewById<TextView>(R.id.btn_send_hex)
        editTextTextPersonName = view.findViewById<EditText>(R.id.editTextTextPersonName)
        val editTextNumber = view.findViewById<EditText>(R.id.editTextNumber)
        editTextNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                setBtn()
                timeType = 0
            }
        })
        val txt_1 = view.findViewById<TextView>(R.id.txt_1)
        val txt1 = view.findViewById<TextView>(R.id.txt1)
        val txt2 = view.findViewById<TextView>(R.id.txt2)
        txt_1.setText(resources.getString(R.string.pattern1))
        selectPopup1 = XPopup.Builder(activity)
            .autoDismiss(false)
            .asCenterList(
                resources.getString(R.string.swMode), arrayOf<String>(
                    resources.getString(R.string.pattern1), resources.getString(R.string.pattern2),
                    resources.getString(R.string.pattern3)
                ), object : OnSelectListener {
                    override fun onSelect(position: Int, text: String) {
                        if (!isLink) {
                            Toast.makeText(
                                activity,
                                resources.getText(R.string.isConnect),
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                        selectPopup1?.dismiss()
                        sendType = position
                        txt_1.setText(text)
                    }
                })
        selectPopup2 = XPopup.Builder(activity)
            .autoDismiss(false)
            .asCenterList(
                resources.getString(R.string.NodeMode), arrayOf<String>(
                    "1", "2", "3", "4", "5", "6"
                ), object : OnSelectListener {
                    override fun onSelect(position: Int, text: String) {
                        selectPopup2?.dismiss()
                        if (!isLink) {
                            Toast.makeText(
                                activity,
                                resources.getText(R.string.isConnect),
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                        txt1.setText(text)
                        send("AT+MOD=" + (position + 1))
                    }
                })
        selectPopup3 = XPopup.Builder(activity)
            .autoDismiss(false)
            .asCenterList(
                resources.getString(R.string.InterruptMode), arrayOf<String>(
                    resources.getString(R.string.Disable),
                    resources.getString(R.string.falling_or_rising),
                    resources.getString(R.string.falling),
                    resources.getString(R.string.rising)
                ), object : OnSelectListener {
                    override fun onSelect(position: Int, text: String) {
                        selectPopup3?.dismiss()
                        if (!isLink) {
                            Toast.makeText(
                                activity,
                                resources.getText(R.string.isConnect),
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                        txt2.setText(text)
                        send("AT+INTMOD=$position")
                    }
                })
        selectPopup4 = XPopup.Builder(activity)
            .autoDismiss(false)
            .asCenterList(
                resources.getString(R.string.text_code), arrayOf<String>(
                    resources.getString(R.string.ascii), resources.getString(R.string.hex)
                ), object : OnSelectListener {
                    override fun onSelect(position: Int, text: String) {
                        selectPopup4?.dismiss()
                        if (position == 0) {
                            seleteHex = false
                            btn_selete.setText(
                                resources.getString(R.string.text_code) + "(" + resources.getString(
                                    R.string.ascii
                                ) + ")"
                            )
                        } else {
                            seleteHex = true
                            btn_selete.setText(
                                resources.getString(R.string.text_code) + "(" + resources.getString(
                                    R.string.hex
                                ) + ")"
                            )
                        }
                        logAdapter!!.setSeleteHex(seleteHex)
                        logAdapter!!.notifyDataSetChanged()
                    }
                })
        selectPopup5 = XPopup.Builder(activity)
            .autoDismiss(false)
            .asCenterList(
                resources.getString(R.string.text_code_send), arrayOf<String>(
                    resources.getString(R.string.ascii), resources.getString(R.string.hex)
                ), object : OnSelectListener {
                    override fun onSelect(position: Int, text: String) {
                        selectPopup5?.dismiss()
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
                })
        selectPopup6 = XPopup.Builder(activity)
            .autoDismiss(false)
            .asCenterList(
                resources.getString(R.string.Common_instructions),
                atList,
                object : OnSelectListener {
                    override fun onSelect(position: Int, text: String) {
                        selectPopup6?.dismiss()
                        txt_atdetails.setText(atDetailsList[position])
                        edittext1.setText(atList[position])
                    }
                })

        view.findViewById<View>(R.id.bnt1).setOnClickListener(this)
        view.findViewById<View>(R.id.txt_1).setOnClickListener(this)
        view.findViewById<View>(R.id.title_left).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_right).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_save).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_send_hex).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_send_code).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_clear_log).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_time).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_selete).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_suspend).setOnClickListener(this)
        view.findViewById<View>(R.id.txt_time1).setOnClickListener(this)
        view.findViewById<View>(R.id.txt_time2).setOnClickListener(this)
        view.findViewById<View>(R.id.txt_time3).setOnClickListener(this)
        view.findViewById<View>(R.id.txt_time4).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_send).setOnClickListener(this)

        txt1.setOnClickListener(this)
        txt2.setOnClickListener(this)
        title = view.findViewById<TextView>(R.id.title)
        view1 = view.findViewById<View>(R.id.view1)
        view2 = view.findViewById<View>(R.id.view2)
        btn_send = view.findViewById<View>(R.id.btn_send)
        val btn_switch = view.findViewById<Switch>(R.id.btn_switch)
        isSwitch = true
        switchrigth(isSwitch!!)
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val listView = view.findViewById<ListView>(R.id.listView)
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL)
        listView.setStackFromBottom(true)
        logAdapter = LogAdapter(requireContext())
        logAdapter!!.setSeleteHex(seleteHex)
        listView.setAdapter(logAdapter)
        swipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                //刷新需执行的操作
//                setble();
            }
        })
        swipeRefreshLayout.setRefreshing(false)

        btn_switch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                Log.e("onCheckedChanged", "$isChecked**")
                val sw = if (isChecked) "1" else "0"
                if (sendType == 0) {
                    send("AT+ADR=$sw")
                }
                if (sendType == 1) {
                    send("AT+CFM=$sw")
                }
                if (sendType == 2) {
                    send("AT+NJM=$sw")
                }
            }
        })

        btn_selete.setText(resources.getString(R.string.text_code) + "(" + resources.getString(R.string.ascii) + ")")
        btn_send_hex.setText(
            resources.getString(R.string.text_code_send) + "(" + resources.getString(
                R.string.ascii
            ) + ")"
        )

        txt_atdetails.setText(atDetailsList[0])
        edittext1.setText(atList[0])
    }

    fun setBtn() {
        txt_time1!!.setBackgroundResource(R.drawable.bg_black)
        txt_time2!!.setBackgroundResource(R.drawable.bg_black)
        txt_time3!!.setBackgroundResource(R.drawable.bg_black)
        txt_time4!!.setBackgroundResource(R.drawable.bg_black)
    }

    private var isSwitch: Boolean? = null

    fun switchrigth(isSwitch: Boolean) {
        if (isSwitch) {
            view1!!.visibility = View.VISIBLE
            view2!!.visibility = View.GONE
            btn_send!!.visibility = View.GONE
        } else {
            view1!!.visibility = View.GONE
            view2!!.visibility = View.VISIBLE
            btn_send!!.visibility = View.VISIBLE
        }
    }


    fun initData() {
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun UODATE(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.UODATE) {
            return
        }
        val device: BleDevice? = event.body
        if (device != null) {
            if (device.getName() != null) {
    //            txt2.setText("已成功连接（" + device.getName() + ")");
            } else {
    //            txt2.setText("已成功连接（" + device.getMac() + ")");
            }
        }
        isLink = true
        auto = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun upData(event: MessageEvent<String?>) {
        if (event.id != EventBusId.upData) {
            return
        }
        if (!isPause) {
            val data = TextUtils.trim(event.body).uppercase(Locale.getDefault())
            logList.add(TextUtils.decode(data))
            logAdapter!!.addResult(logList)
            logAdapter!!.notifyDataSetChanged()
        }
    }

    private var isLink = false

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun UODATE1(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.UODATE1) {
            return
        }
        auto = false
        title?.setText(R.string.text_noconnect)
        isLink = false
        PreferencesUtil.putBoolean(activity, "auto", auto!!)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun deveui(event: MessageEvent<String?>) {
        if (event.id != EventBusId.deveui) {
            return
        }
        title!!.text = event.body
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        Log.e("onDestroy", "onDestroy2")
    }

    var isPause: Boolean = false

    override fun onClick(v: View) {
        if (!isLink) {
            Toast.makeText(activity, resources.getText(R.string.isConnect), Toast.LENGTH_SHORT)
                .show()
            return
        }
        val messageEvent1 = MessageEvent<String>()
        when (v.id) {
            R.id.bnt1 ->                 //
                selectPopup6?.show()

            R.id.txt_1 ->                 //
                selectPopup1?.show()

            R.id.txt1 ->                 //
                selectPopup2?.show()

            R.id.txt2 ->                 //
                selectPopup3?.show()

            R.id.title_left -> { // Restart
                XPopup.Builder(activity)
                    .asConfirm(
                        "",
                        resources.getString(R.string.restart)
                    ) {
                        send("ATZ")
                    }
                    .show()
            }

            R.id.btn_right -> {
                //切换
                isSwitch = !isSwitch!!
                switchrigth(isSwitch!!)
            }

            R.id.btn_clear_log -> { // Clear log
                XPopup.Builder(activity)
                    .asConfirm(
                        "",
                        resources.getString(R.string.isclearlog)
                    ) {
                        clearLog()
                    }
                    .show()
            }

            R.id.btn_save -> { // Save log
                XPopup.Builder(activity)
                    .asConfirm(
                        resources.getString(R.string.save),
                        resources.getString(R.string.issave)
                    ) {
                        saveLogWithPermissionCheck()
                    }
                    .show()
            }

            R.id.btn_suspend ->                 //重启
                pauseLog()

            R.id.btn_send_code ->                 //发送
                sendCode()

            R.id.btn_send ->                 //发送
                sendCode1()

            R.id.btn_send_hex ->                 //发送接收编码
                selectPopup5?.show()

            R.id.btn_selete ->                 //日志接收编码
                selectPopup4?.show()

            R.id.txt_time1 -> {
                //重启
                setBtn()
                v.setBackgroundResource(R.drawable.bg_black_on)
                timeType = 5
            }

            R.id.txt_time2 -> {
                //重启
                setBtn()
                v.setBackgroundResource(R.drawable.bg_black_on)
                timeType = 10
            }

            R.id.txt_time3 -> {
                //重启
                setBtn()
                v.setBackgroundResource(R.drawable.bg_black_on)
                timeType = 20
            }

            R.id.txt_time4 -> {
                //重启
                setBtn()
                v.setBackgroundResource(R.drawable.bg_black_on)
                timeType = 40
            }

            R.id.btn_time ->                 //设置时间
                if (timeType == 0) {
                    if (editTextNumber?.getText().toString().trim { it <= ' ' }.length == 0) {
                        Toast.makeText(
                            activity,
                            resources.getText(R.string.tdcTime),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (editTextNumber?.getText().toString().trim { it <= ' ' }.toInt() == 0) {
                            Toast.makeText(
                                activity,
                                resources.getText(R.string.tdcTime),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        send("AT+TDC=" + editTextNumber?.getText().toString().trim { it <= ' ' }
                            .toInt() * 60 * 1000)
                    }
                } else {
                    send("AT+TDC=" + timeType * 60 * 1000)
                }
        }
    }

    fun clearLog() {
        logList.clear()
        logAdapter!!.addResult(logList)
        logAdapter!!.notifyDataSetChanged()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if all permissions are granted
        if (permissions.all { it.value }) {
            performSaveLog()
        } else {
            Toast.makeText(activity, R.string.permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    fun saveLogWithPermissionCheck() {
        val perms = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (perms.all { ContextCompat.checkSelfPermission(requireActivity(), it) == PackageManager.PERMISSION_GRANTED }) {
            performSaveLog()
        } else {
            // Request permissions
            requestPermissionLauncher.launch(perms)
        }
    }

    private fun performSaveLog() {
        if (logList.isNotEmpty()) {
            var log = ""
            for (i in logList.indices) {
                log += logList[i]
            }
            LogUtil.writerlog(log)
        } else {
            Toast.makeText(activity, resources.getText(R.string.log_Tips), Toast.LENGTH_SHORT).show()
        }
    }

    fun pauseLog() {
        isPause = !isPause
        if (isPause) {
            btn_suspend!!.text = resources.getText(R.string.Pause_reception)
        } else {
            btn_suspend!!.text = resources.getText(R.string.Continue_receiving)
        }
    }


    fun sendCode1() {
        if (edittext1?.getText().toString().trim { it <= ' ' }.length == 0) {
            Toast.makeText(activity, resources.getText(R.string.instructions), Toast.LENGTH_SHORT)
                .show()
        } else {
            send(edittext1?.getText().toString().trim { it <= ' ' })
        }
    }

    fun sendCode() {
        if (editTextTextPersonName?.getText().toString().trim { it <= ' ' }.length == 0) {
            Toast.makeText(activity, resources.getText(R.string.instructions), Toast.LENGTH_SHORT)
                .show()
        } else {
            if (sendHex) {
                send(
                    TextUtils.decode(
                        editTextTextPersonName?.getText().toString().trim { it <= ' ' })
                )
            } else {
                send(editTextTextPersonName?.getText().toString().trim { it <= ' ' })
            }
        }
    }

    private inner class LogAdapter(private val context: Context) : BaseAdapter() {
        private var seleteHex: Boolean? = null
        private var logList: MutableList<String>
        private val mac: String? = null

        init {
            logList = ArrayList()
        }

        fun setSeleteHex(seleteHex: Boolean?) {
            this.seleteHex = seleteHex
        }

        fun addResult(characteristicList: MutableList<String>) {
//            for ( int i=0;i<characteristicList.size();i++ ){
//
//            }
            this.logList = characteristicList
        }

        fun clear() {
            logList.clear()
        }

        override fun getCount(): Int {
            return logList.size
        }

        override fun getItem(position: Int): String? {
            if (position > logList.size) return null
            return logList[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: ViewHolder
            if (convertView != null) {
                holder = convertView.tag as ViewHolder
            } else {
                convertView = View.inflate(context, R.layout.adapter_log, null)
                holder = ViewHolder()
                holder.txt_log = convertView.findViewById<View>(R.id.txt_log) as TextView
                convertView.setTag(holder)
            }
            //            holder.txt_title.setText("数据:");
            if (seleteHex!!) {
                holder.txt_log!!.text =
                    TextUtils.strToASCII(logList[position])
            } else {
                holder.txt_log!!.text = logList[position]
            }


            return convertView!!
        }

        inner class ViewHolder {
            var txt_log: TextView? = null
        }
    }

    fun send(send: String?) {
        val messageEvent1 = MessageEvent<String>()
        messageEvent1.id = EventBusId.send
        messageEvent1.body = send
        EventBus.getDefault().post(messageEvent1)
    }
}