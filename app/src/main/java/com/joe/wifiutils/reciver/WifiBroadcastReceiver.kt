package com.joe.wifiutils.reciver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import com.joe.wifiutils.utils.WifiControlUtils
import android.net.NetworkInfo
import android.net.wifi.SupplicantState
import android.widget.Toast


class WifiBroadcastReceiver : BroadcastReceiver() {

    lateinit var wifiControl: WifiControlUtils
    override fun onReceive(context: Context?, intent: Intent?) {
        wifiControl = WifiControlUtils.get(context!!)
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent!!.getAction())) {
            val wifiState = intent!!.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLING)
            when (wifiState) {
                WifiManager.WIFI_STATE_DISABLED -> {
                }
                WifiManager.WIFI_STATE_DISABLING -> {
                }
                WifiManager.WIFI_STATE_ENABLED -> {
                }
                WifiManager.WIFI_STATE_ENABLING -> {
                }
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            //网络状态改变
            val info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO) as NetworkInfo
            if (NetworkInfo.State.DISCONNECTED == info.getState()) {
                //wifi网络连接断开
                Toast.makeText(context, "wifi 连接断开！", Toast.LENGTH_SHORT).show()
            } else if (NetworkInfo.State.CONNECTED == info.getState()) {
                //获取当前网络，wifi名称
                Toast.makeText(context, "wifi 连接成功！", Toast.LENGTH_SHORT).show()
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                //wifi密码错误广播
                var netNewState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE) as SupplicantState
                //错误码
                var netConnectErrorCode =
                    intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, WifiManager.ERROR_AUTHENTICATING)
                Toast.makeText(context, "wifi 连接错误！errorcode = " + netConnectErrorCode, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
