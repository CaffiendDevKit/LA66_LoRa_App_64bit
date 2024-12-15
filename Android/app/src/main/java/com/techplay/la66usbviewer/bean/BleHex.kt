package com.techplay.la66usbviewer.bean

object BleHex {
    //启动 - Start
    const val START: String = "5A8101000000000000000000000000"

    //停止 - Stop
    const val STOP: String = "5A8100000000000000000000000000"

    //自启电压 - Self-starting voltage
    const val setV: String = "5A8501"

    //输出电压 - Output Voltage
    const val setVMAX: String = "5A8201"

    //自熄电流 - Self-extinguishing current
    const val setI: String = "5A8601"

    //最大限流值 - Maximum Current Limit
    const val setIMAX: String = "5A8301"

    //end
    const val setEnd: String = "00000000000000000000"
}
