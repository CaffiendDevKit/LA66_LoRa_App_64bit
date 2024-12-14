package com.techplay.la66usbviewer.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.clj.fastble.data.BleDevice
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.techplay.la66usbviewer.R
import com.techplay.la66usbviewer.config.EventBusId
import com.techplay.la66usbviewer.utils.PreferencesUtil
import org.greenrobot.eventbus.ThreadMode


class HomeFragment : androidx.fragment.app.Fragment() {
    private lateinit var viewPager: ViewPager2
    // private lateinit var indicatorAdapter: IndicatorAdapter
    private var autoScrollHandler: Handler? = null
    private var autoScrollRunnable: Runnable? = null

    //    private ListView listView;
    //    private MainActivity.ResultAdapter mResultAdapter;
    private var txt_language: android.widget.TextView? = null
    private var title_left: android.view.View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): android.view.View? {
        val view: android.view.View = inflater.inflate(R.layout.fragment_home, container, false)
        org.greenrobot.eventbus.EventBus.getDefault().register(this)
        initView(view)
        initData()


        return view
    }

    private var isEu: Boolean? = null
    private var loadingPopup: BasePopupView? = null

    private var loadingPopup1: BasePopupView? = null

    fun initView(view: android.view.View) {
        viewPager = view.findViewById(R.id.view_pager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)

        // Set up adapter
        val pagerAdapter = MyPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Set up TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = "Page ${position + 1}" // Or use dynamic titles
        }.attach()

        txt_language = view.findViewById(R.id.txt_language)
        title_left = view.findViewById(R.id.title_left)

        val loadingPopup = XPopup.Builder(activity).asLoading("启动中")
        val loadingPopup1 = XPopup.Builder(activity)
            .asConfirm("故障提示", destaStrMain) {
                loadingPopup1?.dismiss()
            }

        swtBanner()
        val isEu = PreferencesUtil.getBoolean(context, "isEu", false)
        txt_language?.text = if (isEu) "eu" else "中文"

        view.findViewById<View>(R.id.btn_right).setOnClickListener {
            PreferencesUtil.putBoolean(context, "isReset", true)
            val messageEvent = com.techplay.la66usbviewer.bean.MessageEvent<BleDevice>().apply {
                id = EventBusId.disconnect
            }
            org.greenrobot.eventbus.EventBus.getDefault().post(messageEvent)
            PreferencesUtil.putBoolean(context, "isEu", !isEu)
            com.techplay.la66usbviewer.utils.LanguageUtil.set(!isEu, activity)
        }

        view.findViewById<View>(R.id.title_left).setOnClickListener {
            val messageEvent1 = com.techplay.la66usbviewer.bean.MessageEvent<BleDevice>().apply {
                id = EventBusId.goSearch
            }
            org.greenrobot.eventbus.EventBus.getDefault().post(messageEvent1)
        }
    }

    private fun swtBanner() {
        // Initialize banner list
        val bannerList = listOf(
            R.mipmap.laq4,
            R.mipmap.lbt1,
            R.mipmap.ldds20,
            R.mipmap.ldds75,
            R.mipmap.lds01,
            R.mipmap.lds02,
            R.mipmap.lgt,
            R.mipmap.lht65,
            R.mipmap.llds12,
            R.mipmap.llms01,
            R.mipmap.lse01,
            R.mipmap.lsn50v2_d20,
            R.mipmap.lsn50v2_s31,
            R.mipmap.lsn50v2_s31b,
            R.mipmap.lsph01,
            R.mipmap.lt_22222,
            R.mipmap.ltc2,
            R.mipmap.lwl01,
            R.mipmap.lwl02,
            R.mipmap.rs485_ln
        )

        // Set up ViewPager2
        viewPager = requireView().findViewById(R.id.view_pager)
        val adapter = BannerAdapter(bannerList) { position ->
            // Handle banner click events
            onBannerClick(position)
        }
        viewPager.adapter = adapter

        // Set up TabLayout for indicators
        val tabLayout = requireView().findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        // Enable auto-scroll
        enableAutoScroll(viewPager, 2000)
    }
    fun initData() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        org.greenrobot.eventbus.EventBus.getDefault().unregister(this)

        android.util.Log.e("onDestroy", "onDestroy1")
    }

    @org.greenrobot.eventbus.Subscribe(threadMode = ThreadMode.MAIN)
    fun upData(event: com.techplay.la66usbviewer.bean.MessageEvent<String?>) {
        if (event.id != EventBusId.upData) {
            return
        }
        val data = com.techplay.la66usbviewer.utils.TextUtils.trim(event.body)
            .uppercase(java.util.Locale.getDefault())
    }

    var destaStrMain: String = ""

    fun str(str: String, str1: String): String {
        android.util.Log.e("datahome1122", str)
        android.util.Log.e("datahome1122", str1)
        return if (str.length > 0) {
            "$str、$str1"
        } else {
            str + str1
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        autoScrollHandler?.removeCallbacks(autoScrollRunnable!!)
    }

    private fun enableAutoScroll(viewPager: ViewPager2, interval: Long) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val currentItem = viewPager.currentItem
                val nextItem = (currentItem + 1) % viewPager.adapter!!.itemCount
                viewPager.setCurrentItem(nextItem, true)
                handler.postDelayed(this, interval)
            }
        }
        handler.postDelayed(runnable, interval)

        // Stop auto-scroll on user interaction
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    handler.removeCallbacks(runnable)
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    handler.postDelayed(runnable, interval)
                }
            }
        })
    }

    private fun onBannerClick(position: Int) {
        when (position) {
            0 -> Log.d("BannerClick", "Clicked on banner at position $position")
            // Add more cases as needed
        }
    }


    class MyPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val items = listOf("Page 1", "Page 2", "Page 3") // Replace with real data

        override fun getItemCount(): Int = items.size

        override fun createFragment(position: Int): Fragment {
            // Replace with your fragments or views for each page
            return PageFragment.newInstance(items[position])
        }
    }

    class PageFragment : Fragment() {

        companion object {
            private const val ARG_PAGE_TITLE = "arg_page_title"

            fun newInstance(title: String): PageFragment {
                val fragment = PageFragment()
                val args = Bundle()
                args.putString(ARG_PAGE_TITLE, title)
                fragment.arguments = args
                return fragment
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_page, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Retrieve the title passed through arguments
            val title = arguments?.getString(ARG_PAGE_TITLE)

            // Set up the view (e.g., display the title in a TextView)
            val textView: TextView = view.findViewById(R.id.page_title)
            textView.text = title
        }
    }


    class BannerAdapter(
        private val bannerList: List<Int>,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_banner, parent, false)
            return BannerViewHolder(view)
        }

        override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
            holder.bind(bannerList[position])
            holder.itemView.setOnClickListener { onClick(position) }
        }

        override fun getItemCount(): Int = bannerList.size

        class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.iv_banner)

            fun bind(resId: Int) {
                imageView.setImageResource(resId)
            }
        }
    }

}
