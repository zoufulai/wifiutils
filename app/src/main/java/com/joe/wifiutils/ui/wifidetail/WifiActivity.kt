package com.joe.wifiutils.ui.wifidetail

import com.joe.wifiutils.R
import com.joe.wifiutils.base.BaseActivity
import com.joe.wifiutils.utils.WifiControlUtils
import kotlinx.android.synthetic.main.activity_wifi.*

class WifiActivity : BaseActivity() {
    override val layoutId: Int = R.layout.activity_wifi

    override fun init() {
        var wifiControlUtils = WifiControlUtils.get(applicationContext)
        wifi_tv.text = wifiControlUtils.getWifiInfo()
    }

}
