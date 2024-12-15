package com.techplay.la66usbviewer.adapter
/*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import com.techplay.la66usbviewer.R

class MallPagerAdapter(private val list: List<Int>, private val context: Context) :
    PagerAdapter() {
    val count: Int
        get() = list.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return count
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val linearLayout: LinearLayout = LayoutInflater.from(container.context)
            .inflate(R.layout.item_banner, null) as LinearLayout
        val imageView: ImageView = linearLayout.findViewById<ImageView>(R.id.iv_banner)
        imageView.setImageResource(list[position])
        container.addView(linearLayout)
        linearLayout.setOnClickListener(View.OnClickListener { })

        return linearLayout
    }

    fun openBrowser(url: String?) {
        if (url == null) {
            return
        }
        if (url.length == 0) {
            return
        }
        val intent = Intent()
        intent.setAction(Intent.ACTION_VIEW)
        if (url.indexOf("http") == -1) {
            intent.setData(Uri.parse("https://$url"))
        } else {
            intent.setData(Uri.parse(url))
        }
        // 注意此处的判断intent.resolveActivity()可以返回显示该Intent的Activity对应的组件名
        // 官方解释 : Name of the component implementing an activity that can display the intent
        if (intent.resolveActivity(context.packageManager) != null) {
            val componentName: ComponentName = intent.resolveActivity(context.packageManager)
            Log.e("suyan = ", componentName.getClassName() + "")
            context.startActivity(Intent.createChooser(intent, "请选择浏览器"))
        } else {
            Toast.makeText(context, "链接错误或无浏览器", Toast.LENGTH_LONG).show()
            //            showText("链接错误或无浏览器");
//            GlobalMethod.showToast(context, "链接错误或无浏览器");
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view: LinearLayout = `object` as LinearLayout
        container.removeView(view)
    }

    private var onBannerClickListener: OnBannerClickListener? = null

    interface OnBannerClickListener {
        fun onClick(position: Int)
    }

    fun setOnBannerClickListener(onBannerClickListener: OnBannerClickListener?) {
        this.onBannerClickListener = onBannerClickListener
    }
}

*/