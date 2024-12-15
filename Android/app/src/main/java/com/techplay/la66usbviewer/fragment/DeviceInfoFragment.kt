package com.techplay.la66usbviewer.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.clj.fastble.data.BleDevice
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.techplay.la66usbviewer.R
import com.techplay.la66usbviewer.bean.MessageEvent
import com.techplay.la66usbviewer.config.EventBusId
import com.techplay.la66usbviewer.utils.PreferencesUtil
import com.techplay.la66usbviewer.utils.TextUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Locale

class DeviceInfoFragment : Fragment() {
    private var txt_1: TextView? = null
    private var txt_2: TextView? = null
    private var txt_3: TextView? = null
    private var txt_4: TextView? = null
    private var txt_5: TextView? = null
    private var txt_6: TextView? = null
    private var txt_7: TextView? = null
    private var txt_8: TextView? = null
    private var txt_9: TextView? = null
    private var txt_10: TextView? = null
    private var txt_11: TextView? = null
    private var txt_12: TextView? = null
    private var txt_13: TextView? = null
    private var txt_14: TextView? = null
    private var txt_15: TextView? = null
    private var txt_16: TextView? = null
    private var txt1: TextView? = null
    private var txt2: TextView? = null
    private var img_11: ImageView? = null
    private var editTextNumber: EditText? = null
    private var pList = ArrayList<String>()
    private var listView: ListView? = null
    private var passwordAdapter: PasswordAdapter? = null
    private var isFrist = true
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_info, container, false)
        EventBus.getDefault().register(this)
        initView(view)
        //        initData();
        Log.e("onCreateView11", "onCreateView222")
        return view
    }


    private fun initView(view: View) {
        val listView = view.findViewById<ListView>(R.id.listView)
        passwordAdapter = PasswordAdapter(context, object : Odclick {
            override fun onPwClick(str: String?) {
                editTextNumber?.setText(str)
                listView.setVisibility(View.INVISIBLE)
            }
        })
        listView.setAdapter(passwordAdapter)
        listView.setVisibility(View.INVISIBLE)
        txt_1 = view.findViewById<TextView>(R.id.txt_1)
        txt_2 = view.findViewById<TextView>(R.id.txt_2)
        txt_3 = view.findViewById<TextView>(R.id.txt_3)
        txt_4 = view.findViewById<TextView>(R.id.txt_4)
        txt_5 = view.findViewById<TextView>(R.id.txt_5)
        txt_6 = view.findViewById<TextView>(R.id.txt_6)
        txt_7 = view.findViewById<TextView>(R.id.txt_7)
        txt_8 = view.findViewById<TextView>(R.id.txt_8)
        txt_9 = view.findViewById<TextView>(R.id.txt_9)
        txt_10 = view.findViewById<TextView>(R.id.txt_10)
        txt_11 = view.findViewById<TextView>(R.id.txt_11)
        txt_12 = view.findViewById<TextView>(R.id.txt_12)
        txt_13 = view.findViewById<TextView>(R.id.txt_13)
        txt_14 = view.findViewById<TextView>(R.id.txt_14)
        txt_15 = view.findViewById<TextView>(R.id.txt_15)
        txt_16 = view.findViewById<TextView>(R.id.txt_16)
        val txt1 = view.findViewById<TextView>(R.id.txt1)
        txt2 = view.findViewById<TextView>(R.id.txt2)
        img_11 = view.findViewById<ImageView>(R.id.img_11)
        val editTextNumber = view.findViewById<EditText>(R.id.editTextNumber)
        view.findViewById<View>(R.id.title_left).setOnClickListener {
            val messageEvent: MessageEvent<BleDevice> =
                MessageEvent<BleDevice>()
            messageEvent.id = EventBusId.dislink
            EventBus.getDefault().post(messageEvent)
        }
        val password: String = PreferencesUtil.getString(activity, "password")
        val passwordL: String = PreferencesUtil.getString(activity, "passwordList")
        editTextNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                Log.e("afterTextChanged", "56")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("afterTextChanged", "57")
            }

            override fun afterTextChanged(s: Editable) {
                Log.e("afterTextChanged", "52")
                if (!isFrist) {
                    listView.setVisibility(View.VISIBLE)
                } else {
                    isFrist = false
                }
            }
        })
        editTextNumber.setText(password)
        if (passwordL != null && passwordL.length > 0) {
            pList = Gson().fromJson(passwordL, object : TypeToken<List<String?>?>() {}.getType())
        }
        passwordAdapter!!.addResult(pList)
        passwordAdapter!!.notifyDataSetChanged()
        view.findViewById<View>(R.id.btn_send_code).setOnClickListener {
            listView.setVisibility(View.INVISIBLE)
            if (editTextNumber.getText().toString().length > 0) {
                val password: String = editTextNumber.getText().toString()
                val messageEvent1 = MessageEvent<String>()
                messageEvent1.id = EventBusId.send
                messageEvent1.body = password
                EventBus.getDefault().post(messageEvent1)
                PreferencesUtil.putString(
                    activity,
                    "password",
                    (editTextNumber.getText().toString())
                )
                Log.e("pList.size()", pList.size.toString() + "**")
                if (pList.size == 5) {
                    pList.add(0, password)
                    pList.removeAt(pList.size - 1)
                } else {
                    pList.add(0, password)
                }
                Log.e("pList.size()", pList.size.toString() + "**")
                passwordAdapter!!.addResult(pList)
                passwordAdapter!!.notifyDataSetChanged()
                PreferencesUtil.putString(activity, "passwordList", Gson().toJson(pList))
            } else {
                Toast.makeText(
                    activity,
                    resources.getString(R.string.placePassword),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        txt1.setText(R.string.no_connected)
        view.findViewById<View>(R.id.btn_right).setOnClickListener(View.OnClickListener {
            if (!isLink) {
                Toast.makeText(activity, resources.getText(R.string.isConnect), Toast.LENGTH_SHORT)
                    .show()
                return@OnClickListener
            }
            val messageEvent1 = MessageEvent<String>()
            messageEvent1.id = EventBusId.send
            messageEvent1.body = "AT+CFG"
            EventBus.getDefault().post(messageEvent1)
        })
    }

    private val isSend = false

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun deviceDetails(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.deviceDetails) {
            return
        }
        if (isLink) {
            val messageEvent1 = MessageEvent<String>()
            messageEvent1.id = EventBusId.send
            messageEvent1.body = "AT+CFG"
            EventBus.getDefault().post(messageEvent1)
        }
        if (!isLink) {
            Toast.makeText(activity, resources.getText(R.string.isConnect), Toast.LENGTH_SHORT)
                .show()
        }
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
        val messageEvent1 = MessageEvent<String>()
        messageEvent1.id = EventBusId.send
        messageEvent1.body = "AT+CFG"
        EventBus.getDefault().post(messageEvent1)
        isLink = true
        //        auto = true;
    }

    private var isLink = true

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun UODATE1(event: MessageEvent<BleDevice?>) {
        if (event.id != EventBusId.UODATE1) {
            return
        }
        //        auto = false;
//        txt2.setText("未连接");
        txt1?.setText(R.string.text_noconnect)
        isLink = false
        txt2!!.text = "Deveui"
        txt_1!!.text = "0"
        txt_2!!.text = "0"
        txt_3!!.text = "0"
        txt_4!!.text = "0"
        txt_5!!.text = "0"
        txt_6!!.text = "0"
        txt_7!!.text = "0"
        txt_8!!.text = "0"
        txt_9!!.text = "0"
        txt_10!!.text = "0"
        txt_11!!.text = "0"
        txt_12!!.text = "0"
        txt_13!!.text = "0"
        txt_14!!.text = "0"
        txt_15!!.text = "0"
        txt_16!!.text = "0"
        //        PreferencesUtil.putBoolean(getActivity(), "auto", auto);
    }

    private var isConfing = false
    private var config = ""
    private val config1 =
        "53 74 6F 70 20 54 78 20 65 76 65 6E 74 73 2C 50 6C 65 61 73 65 20 77 61 69 74 20 66 6F 72 20 61 6C 6C 20 63 6F 6E 66 69 67 75 72 61 74 69 6F 6E 73 20 74 6F 20 70 72 69 6E 74 0D 50 72 69 6E 74 66 20 61 6C 6C 20 63 6F 6E 66 69 67 2E 2E 2E 0D 41 54 2B 44 45 55 49 3D 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 0D 0A 41 54 2B 44 41 44 44 52 3D 30 31 30 31 30 31 30 31 0A 0D 41 54 2B 41 50 50 4B 45 59 3D 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 0D 0A 41 54 2B 4E 57 4B 53 4B 45 59 3D 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 0D 0A 41 54 2B 41 50 50 53 4B 45 59 3D 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 0D 0A 41 54 2B 41 50 50 45 55 49 3D 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 20 30 31 0D 0A 41 54 2B 41 44 52 3D 30 0D 0A 41 54 2B 54 58 50 3D 30 0D 0A 41 54 2B 44 52 3D 30 0D 0A 41 54 2B 44 43 53 3D 30 0D 0A 41 54 2B 50 4E 4D 3D 30 0D 0A 41 54 2B 52 58 32 46 51 3D 30 0D 0A 41 54 2B 52 58 32 44 52 3D 30 0D 0A 41 54 2B 52 58 31 44 4C 3D 30 0D 0A 41 54 2B 52 58 32 44 4C 3D 30 0D 0A 41 54 2B 4A 4E 31 44 4C 3D 30 0D 0A 41 54 2B 4A 4E 32 44 4C 3D 30 0D 0A 41 54 2B 4E 4A 4D 3D 31 0D 0A 41 54 2B 4E 57 4B 49 44 3D 30 30 20 30 30 20 30 30 20 30 30 0D 0A 41 54 2B 46 43 55 3D 30 0D 0A 41 54 2B 46 43 44 3D 30 0D 0A 41 54 2B 43 4C 41 53 53 3D 41 0D 0A 41 54 2B 4E 4A 53 3D 30 0D 0A 41 54 2B 52 45 43 56 42 3D 30 3A 0D 0A 41 54 2B 52 45 43 56 3D 30 3A 0D 0A 41 54 2B 56 45 52 3D 76 31 2E 31 20 41 53 39 32 33 0A 0D 41 54 2B 43 46 4D 3D 30 0D 0A 41 54 2B 43 46 53 3D 30 0D 0A 41 54 2B 53 4E 52 3D 30 0D 0A 41 54 2B 52 53 53 49 3D 30 0D 0A 41 54 2B 54 44 43 3D 33 30 30 30 30 30 0D 0A 41 54 2B 50 4F 52 54 3D 30 0D 0A 41 54 2B 50 57 4F 52 44 3D 30 0D 0A 41 54 2B 43 48 53 3D 30 0D 0A 41 54 2B 53 4C 45 45 50 3D 30 0D 0A 41 54 2B 45 58 54 3D 31 0D 0A 41 54 2B 42 41 54 3D 33 36 37 33 0D 0A 41 54 2B 57 4D 4F 44 3D 30 0D 0A 41 54 2B 41 52 54 45 4D 50 3D 2D 32 30 30 2C 38 30 30 2C 2D 32 30 30 2C 38 30 30 0D 0A 41 54 2B 43 49 54 45 4D 50 3D 31 0D 0A 41 54 2B 44 57 45 4C 4C 54 3D 30 0D 0A 41 54 2B 52 4A 54 44 43 3D 32 30 0D 0A 41 54 2B 52 50 4C 3D 30 0D 0A 41 54 2B 54 49 4D 45 53 54 41 4D 50 3D 73 79 73 74 69 6D 65 3D 20 31 36 31 31 38 38 32 30 31 31 20 32 30 32 31 20 31 20 32 39 20 31 20 30 20 31 31 0A 0D 41 54 2B 4C 45 41 50 53 45 43 3D 30 0D 0A 41 54 2B 53 59 4E 43 4D 4F 44 3D 30 0D 0A 41 54 2B 53 59 4E 43 54 44 43 3D 30 0D 0A 41 54 2B 52 43 41 4C 3D 73 65 67 6D 65 6E 74 20 30 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 31 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 32 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 33 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 34 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 35 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 36 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 37 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 38 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 39 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 31 30 3D 30 20 30 0D 73 65 67 6D 65 6E 74 20 31 31 3D 30 20 30 0D 41 54 2B 52 43 41 42 4C 45 3D 30 2E 30 30 30 20 30 2E 30 30 30 0A 0D 41 54 2B 45 4E 50 54 43 48 4E 55 4D 3D 30 0D 0A 53 74 61 72 74 20 54 78 20 65 76 65 6E 74 73 0D "

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun upConfig(event: MessageEvent<String?>) {
        if (event.id != EventBusId.upConfig) {
            return
        }
        val data = TextUtils.trim(event.body).uppercase(Locale.getDefault())
        Log.e("upConfigdata", data)
        Log.e("upConfig", "upConfig")
        Log.e("upConfig1", TextUtils.isStart(data).toString() + "")
        Log.e("upConfig2", TextUtils.isStop(data).toString() + "")
        if (isConfing) {
            config = config + data
        }
        if (TextUtils.isStart(data)) {
            isConfing = TextUtils.isStart(data)
            config = ""
        }
        if (TextUtils.isStop(data)) {
            isConfing = TextUtils.isStart(data)
            config = config + data
            //            config =  config1.toString().replace(" ","");
            setData()
        }
        if (isConfing) {
            config = config + data
        }
    }

    fun setData() {
        Log.e("setData", config)
        try {
            txt1!!.text =
                TextUtils.value(config, "AT+MODEL=")
                    .split(",".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            img_11!!.setImageResource(
                TextUtils.getImg(
                    TextUtils.value(config, "AT+MODEL=")
                        .split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                )
            )
        } catch (e: Exception) {
            txt1!!.text = resources.getText(R.string.unknown)
        }
        txt2!!.text = TextUtils.value(config, "AT+DEUI=")
        try {
            txt_1!!.text = (TextUtils.value(config, "AT+TDC=")
                .toInt() / 1000).toString() + "s"
        } catch (e: Exception) {
            txt1!!.text = "0"
        }
        txt_2!!.text = TextUtils.value1(config, "AT+VER=", 0)
        txt_3!!.text = if (TextUtils.value(
                config,
                "AT+NJM="
            ) == "1"
        ) "OTAA" else "ABP"
        txt_4!!.text = TextUtils.value(config, "AT+MOD=")
        txt_5!!.text = resources.getText(R.string.shanghang)
            .toString() + TextUtils.value(
            config,
            "AT+FCU="
        ) + "\n" + resources.getText(R.string.xiahang) + TextUtils.value(
            config,
            "AT+FCD="
        )
        txt_6!!.text = TextUtils.value1(config, "AT+VER=", 1)
        txt_7!!.text = if (TextUtils.value(
                config,
                "AT+NJS="
            ) == "1"
        ) resources.getText(R.string.on_connet) else resources.getText(R.string.un_connet)
        txt_8!!.text = if (TextUtils.value(
                config,
                "AT+ADR="
            ) == "1"
        ) resources.getText(R.string.open) else resources.getText(R.string.close)
        txt_9!!.text = TextUtils.value(config, "AT+DR=")
        txt_10!!.text = TextUtils.getINTMOD(
            TextUtils.value(
                config,
                "AT+INTMOD="
            )
        )
        txt_11!!.text = if (TextUtils.value(
                config,
                "AT+CFM="
            ) == "1"
        ) resources.getText(R.string.open) else resources.getText(R.string.close)
        txt_12!!.text = TextUtils.value(config, "AT+TXP=")
        txt_13!!.text = TextUtils.value(config, "AT+5VT=")
        txt_14!!.text = TextUtils.value(config, "AT+CHE=")
        txt_15!!.text = TextUtils.value(config, "AT+CLASS=")
        txt_16!!.text = TextUtils.value(config, "AT+CHS=") + "Hz"
        val messageEvent1 = MessageEvent<String>()
        messageEvent1.id = EventBusId.deveui
        messageEvent1.body = TextUtils.value(config, "AT+DEUI=")
        EventBus.getDefault().post(messageEvent1)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        Log.e("onDestroy", "onDestroy4")
    }

    private inner class PasswordAdapter(
        private val context: Context?,
        private val odclick: Odclick
    ) :
        BaseAdapter() {
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
                convertView = View.inflate(context, R.layout.adapter_log_file1, null)
                holder = ViewHolder()
                holder.txt_log = convertView.findViewById<View>(R.id.txt_log) as TextView
                holder.ln = convertView.findViewById<View>(R.id.ln) as View
                convertView.setTag(holder)
            }
            //            holder.txt_title.setText("数据:");
            holder.txt_log!!.text = logList[position]
            holder.txt_log!!.tag = logList[position]
            holder.txt_log!!.setOnClickListener { v ->
                odclick.onPwClick(v.tag as String)
                //                    String path = (String) v.getTag();
                //                    Intent intent = new Intent(Intent.ACTION_VIEW);
                //                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                //                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //                    Uri uri = Uri.fromFile(new File(LogUtil.getFilePath(path)));
                //                    intent.setDataAndType(uri, "text/plain");
                //                    context.startActivity(intent);
            }

            return convertView!!
        }

        inner class ViewHolder {
            var txt_log: TextView? = null
            var ln: View? = null
        }
    }

    internal interface Odclick {
        fun onPwClick(str: String?)
    }
}
