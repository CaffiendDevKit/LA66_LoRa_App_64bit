package com.techplay.la66usbviewer.fragment

import com.techplay.la66usbviewer.R

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.techplay.la66usbviewer.bean.MessageEvent
import com.techplay.la66usbviewer.utils.LogUtil

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.io.File
import java.util.Locale

import com.lxj.xpopup.XPopup.Builder
import com.lxj.xpopup.interfaces.OnConfirmListener
import com.lxj.xpopup.interfaces.OnInputConfirmListener
import com.techplay.la66usbviewer.config.EventBusId
import com.techplay.la66usbviewer.utils.PreferencesUtil
import com.techplay.la66usbviewer.utils.logFile

// Replace with ActivityResultContracts
// import pub.devrel.easypermissions.EasyPermissions


class LogFragment : Fragment() {
    private var listView: ListView? = null
    var logList: MutableList<logFile> = ArrayList<logFile>()
    private var logAdapter: LogAdapter? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if all permissions are granted
        if (permissions.all { it.value }) {
            setdata() // Perform the action requiring permissions
        } else {
            Toast.makeText(
                requireContext(),
                R.string.permission_denied, // Add a string resource for "Permission denied"
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_setting, container, false)
        EventBus.getDefault().register(this)

        initView(view)
        initData()
        Log.e("onCreateView11", "onCreateView222")
        return view
    }

    fun initView(view: View) {
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val listView = view.findViewById<ListView>(R.id.listView)
        logAdapter = LogAdapter(requireContext())
        listView.adapter = logAdapter

        // Set up refresh listener
        swipeRefreshLayout.setOnRefreshListener {
            setdata() // Execute refresh operation
        }

        // Permissions required
        val perms = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        // Check if permissions are granted
        if (perms.all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }) {
            setdata() // Perform the operation if permissions are already granted
        } else {
            // Request permissions
            requestPermissionsLauncher.launch(perms)
        }
    }

    fun setdata() {
        logList.clear()
        logList.addAll(LogUtil.getAllDataFileName())
        logAdapter!!.addResult(logList)
        logAdapter!!.notifyDataSetChanged()
        swipeRefreshLayout?.setRefreshing(false)
    }


    fun initData() {
        var timeAll: Int = PreferencesUtil.getInt(context, "timeAll", 0)
        val time: Int = PreferencesUtil.getInt(context, "time", 0)
        timeAll = timeAll + time
        PreferencesUtil.putInt(context, "timeAll", timeAll)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        Log.e("onDestroy", "onDestroy3")
        swipeRefreshLayout?.setRefreshing(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }


    private val timeAll = 0

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun upData(event: MessageEvent<String?>) {
        if (event.id != EventBusId.upData) {
            return
        }
    }


    private inner class LogAdapter(private val context: Context) : BaseAdapter() {
        private var seleteHex: Boolean? = null
        private var logList: MutableList<logFile>
        private val mac: String? = null

        init {
            logList = ArrayList<logFile>()
        }

        fun setSeleteHex(seleteHex: Boolean?) {
            this.seleteHex = seleteHex
        }

        fun addResult(characteristicList: MutableList<logFile>) {
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

        override fun getItem(position: Int): logFile? {
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
                convertView = View.inflate(context, R.layout.adapter_log_file, null)
                holder = ViewHolder()
                holder.txt_log = convertView.findViewById<View>(R.id.txt_log) as TextView
                holder.btn_selete = convertView.findViewById<View>(R.id.btn_selete) as TextView
                convertView.setTag(holder)
            }
            //            holder.txt_title.setText("数据:");
            holder.txt_log?.setText(logList[position].getName())
            holder.txt_log!!.tag = logList[position].getPath()
            holder.txt_log!!.setOnClickListener { v ->
                val path = v.tag as String
                //                    Intent intent = new Intent(Intent.ACTION_VIEW);
                //                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                //                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //                    Uri uri = Uri.fromFile(new File(LogUtil.getFilePath(path)));
                //                    intent.setDataAndType(uri, "text/plain");
                //                    context.startActivity(intent);
                openFile(path)
            }
            holder.btn_selete!!.tag = logList[position].getPath()
            holder.btn_selete!!.setOnClickListener { v ->
                val path = v.tag as String
                //                    Intent intent = new Intent(Intent.ACTION_VIEW);
                //                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                //                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //                    Uri uri = Uri.fromFile(new File(LogUtil.getFilePath(path)));
                //                    intent.setDataAndType(uri, "text/plain");
                //                    context.startActivity(intent);
                showDelete(path)
            }


            return convertView!!
        }

        inner class ViewHolder {
            var txt_log: TextView? = null
            var btn_selete: TextView? = null
        }
    }

    fun showEdittex(delFile: String) {
        val perms = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        // Check if permissions are granted
        if (perms.all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }) {
            showInputDialog(delFile)
        } else {
            // Request permissions
            requestPermissionsLauncher.launch(perms)
        }
    }

    private fun showInputDialog(delFile: String) {
        Builder(requireContext()).asInputConfirm(
            resources.getString(R.string.deteleLog),
            resources.getString(R.string.file)
        ) { text ->
            if (text.isNotEmpty()) {
                renameFile(delFile, text)
            } else {
                Toast.makeText(requireContext(), R.string.textEmpty, Toast.LENGTH_SHORT).show()
            }
        }.show()
    }

    fun showDelete(delFile: String) {
        val perms = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        // Check if permissions are granted
        if (perms.all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }) {
            showDeleteDialog(delFile)
        } else {
            // Request permissions
            requestPermissionsLauncher.launch(perms)
        }
    }

    private fun showDeleteDialog(delFile: String) {
        Builder(requireContext()).asConfirm(
            resources.getString(R.string.deteleLog),
            resources.getString(R.string.deleteDevice)
        ) {
            if (delete(delFile)) {
                setdata()
            }
        }.show()
    }

    /**
     * oldPath 和 newPath必须是新旧文件的绝对路径
     */
    private fun renameFile(oldPath: String, newPath: String) {
        val oldFile = File(oldPath)
        if (oldFile.exists()) {
            val filePath = Environment.getExternalStorageDirectory().absolutePath
            val dir = File(filePath, "bleLog")
            val newFile = File(dir, "$newPath.txt")
            val b = oldFile.renameTo(newFile)
            val file2 = File(newFile.path)
            setdata()
        } else {
            Toast.makeText(activity, R.string.fileIsdelete, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param delFile 要删除的文件夹或文件名
     * @return 删除成功返回true，否则返回false
     */
    fun delete(delFile: String): Boolean {
        val file = File(delFile)
        if (!file.exists()) {
//            Toast.makeText(HnUiUtils.getContext(), "删除文件失败:" + delFile + "不存在！", Toast.LENGTH_SHORT).show();
            Log.e("删除文件失败:", delFile + "不存在！")
            return false
        } else {
            return if (file.isFile) deleteSingleFile(delFile)
            else deleteDirectory(delFile)
        }
    }

    /**
     * 获取对应文件的Uri
     * @param intent 相应的Intent
     * @param file 文件对象
     * @return
     */
    private fun getUri(intent: Intent, file: File): Uri? {
        var uri: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //判断版本是否在7.0以上 - Determine if the version is above 7.0
            uri =
                FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".fileprovider",
                    file
                )
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            // Adding this line indicates temporary authorisation of the file represented by this Uri to the target application
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            uri = Uri.fromFile(file)
        }
        return uri
    }

    /**
     * 打开文件
     * @param filePath 文件的全路径，包括到文件名
     */
    private fun openFile(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            //如果文件不存在
            Toast.makeText(context, "打开失败，原因：文件已经被移动或者删除", Toast.LENGTH_SHORT)
                .show()
            return
        }
        /* 取得扩展名 */
        val end = file.name.substring(file.name.lastIndexOf(".") + 1, file.name.length).lowercase(
            Locale.getDefault()
        )
        /* 依扩展名的类型决定MimeType */
        var intent: Intent? = null
        intent =
            if (end == "m4a" || end == "mp3" || end == "mid" || end == "xmf" || end == "ogg" || end == "wav") {
                generateVideoAudioIntent(filePath, DATA_TYPE_AUDIO)
            } else if (end == "3gp" || end == "mp4") {
                generateVideoAudioIntent(filePath, DATA_TYPE_VIDEO)
            } else if (end == "jpg" || end == "gif" || end == "png" || end == "jpeg" || end == "bmp") {
                generateCommonIntent(filePath, DATA_TYPE_IMAGE)
            } else if (end == "apk") {
                generateCommonIntent(filePath, DATA_TYPE_APK)
            } else if (end == "ppt") {
                generateCommonIntent(filePath, DATA_TYPE_PPT)
            } else if (end == "xls") {
                generateCommonIntent(filePath, DATA_TYPE_EXCEL)
            } else if (end == "doc") {
                generateCommonIntent(filePath, DATA_TYPE_WORD)
            } else if (end == "pdf") {
                generateCommonIntent(filePath, DATA_TYPE_PDF)
            } else if (end == "chm") {
                generateCommonIntent(filePath, DATA_TYPE_CHM)
            } else if (end == "txt") {
                generateCommonIntent(filePath, DATA_TYPE_TXT)
            } else {
                generateCommonIntent(filePath, DATA_TYPE_ALL)
            }
        requireContext().startActivity(intent)
    }

    /**
     * 产生除了视频、音频、网页文件外，打开其他类型文件的Intent
     * @param filePath 文件路径
     * @param dataType 文件类型
     * @return
     */
    private fun generateCommonIntent(filePath: String, dataType: String): Intent {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setAction(Intent.ACTION_VIEW)
        val file = File(filePath)
        val uri = getUri(intent, file)
        intent.setDataAndType(uri, dataType)
        return intent
    }

    /**
     * 产生打开视频或音频的Intent
     * @param filePath 文件路径
     * @param dataType 文件类型
     * @return
     */
    private fun generateVideoAudioIntent(filePath: String, dataType: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("oneshot", 0)
        intent.putExtra("configchange", 0)
        val file = File(filePath)
        intent.setDataAndType(getUri(intent, file), dataType)
        return intent
    }

    /**
     * 产生打开网页文件的Intent
     * @param filePath 文件路径
     * @return
     */
    private fun generateHtmlFileIntent(filePath: String): Intent {
        val uri = Uri.parse(filePath)
            .buildUpon()
            .encodedAuthority("com.android.htmlfileprovider")
            .scheme("content")
            .encodedPath(filePath)
            .build()
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, DATA_TYPE_HTML)
        return intent
    }

    companion object {
        /**
         * Deletes a single file.
         *
         * @param filePathName The name of the file to be deleted.
         * @return Returns true if the file was successfully deleted, otherwise false.
         */
        fun deleteSingleFile(`filePath$Name`: String): Boolean {
            val file = File(`filePath$Name`)
            // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
            if (file.exists() && file.isFile) {
                if (file.delete()) {
                    Log.e(
                        "--Method--",
                        "Copy_Delete.deleteSingleFile: 删除单个文件" + `filePath$Name` + "成功！"
                    )

                    return true
                } else {
                    Log.e("删除单个文件", `filePath$Name` + "失败！")
                    return false
                }
            } else {
                Log.e("删除单个文件失败：", `filePath$Name` + "不存在！")
                return false
            }
        }

        /**
         * 删除目录及目录下的文件
         *
         * @param filePath 要删除的目录的文件路径
         * @return 目录删除成功返回true，否则返回false
         */
        fun deleteDirectory(filePath: String): Boolean {
            // 如果dir不以文件分隔符结尾，自动添加文件分隔符
            var filePath = filePath
            if (!filePath.endsWith(File.separator)) filePath = filePath + File.separator
            val dirFile = File(filePath)
            // 如果dir对应的文件不存在，或者不是一个目录，则退出
            if ((!dirFile.exists()) || (!dirFile.isDirectory)) {
                Log.e("删除目录失败：", filePath + "不存在！")
                return false
            }
            var flag = true
            // 删除文件夹中的所有文件包括子目录
            val files = dirFile.listFiles()
            for (file in files!!) {
                // 删除子文件
                if (file.isFile) {
                    flag = deleteSingleFile(file.absolutePath)
                    if (!flag) break
                } else if (file.isDirectory) {
                    flag = deleteDirectory(
                        file
                            .absolutePath
                    )
                    if (!flag) break
                }
            }
            if (!flag) {
                Log.e("删除目录失败！", "544")
                return false
            }
            // 删除当前目录
            if (dirFile.delete()) {
                Log.e("--Method--", "Copy_Delete.deleteDirectory: 删除目录" + filePath + "成功！")
                return true
            } else {
                Log.e("删除目录：", filePath + "失败！")
                return false
            }
        }

        /**声明各种类型文件的dataType */
        private const val DATA_TYPE_ALL = "*/*" //未指定明确的文件类型，不能使用精确类型的工具打开，需要用户选择
        private const val DATA_TYPE_APK = "application/vnd.android.package-archive"
        private const val DATA_TYPE_VIDEO = "video/*"
        private const val DATA_TYPE_AUDIO = "audio/*"
        private const val DATA_TYPE_HTML = "text/html"
        private const val DATA_TYPE_IMAGE = "image/*"
        private const val DATA_TYPE_PPT = "application/vnd.ms-powerpoint"
        private const val DATA_TYPE_EXCEL = "application/vnd.ms-excel"
        private const val DATA_TYPE_WORD = "application/msword"
        private const val DATA_TYPE_CHM = "application/x-chm"
        private const val DATA_TYPE_TXT = "text/plain"
        private const val DATA_TYPE_PDF = "application/pdf"
    }
}

