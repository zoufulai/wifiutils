package com.joe.wifiutils.utils

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.support.v4.content.ContextCompat.getSystemService
import android.text.TextUtils
import android.util.Log

class WifiControlUtils private constructor(context: Context) {

    companion object {
        private var wifiControlUtils: WifiControlUtils? = null
        fun get(context: Context): WifiControlUtils {
            if (wifiControlUtils == null) {
                wifiControlUtils = WifiControlUtils(context)
            }
            return wifiControlUtils as WifiControlUtils
        }
    }

    var mWifiManager: WifiManager
    private lateinit var mWifiLock: WifiManager.WifiLock
    // 网络连接列表
    private lateinit var mWifiConfiguration: List<WifiConfiguration>
    private lateinit var mWifiList: List<ScanResult>

    // 定义WifiInfo对象
    private var mWifiInfo: WifiInfo

    init {
        mWifiManager = context.getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        mWifiInfo = mWifiManager.getConnectionInfo()
    }

    /**
     * 打开wifi
     */
    fun openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true)
        }
    }

    /**
     * 关闭wifi
     */
    fun closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false)
        }
    }

    /**
     * 检查wifi状态
     */
    fun checkState(): Int {
        return mWifiManager.getWifiState()
    }

    /**
     * 锁定wifi
     */
    fun acquireWifiLock() {
        mWifiLock.acquire()
    }

    /**
     * 解锁wifi
     */
    fun releaseWifiLock() {
        // 判断时候锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire()
        }
    }

    /**
     * 创建一个WifiLock
     */
    fun creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("clock")
    }


    /**
     * 获取配置好的wifi
     */
    fun getConfiguration(): List<WifiConfiguration> {
        return mWifiConfiguration
    }


    /**
     * 指定配置好的网络进行连接
     */
    fun getConfiguration(index: Int) {
        // 索引大于配置好的网络索引返回
        if (index > mWifiConfiguration.size) {
            return
        }
        // 连接配置好的指定ID的网络
        mWifiManager.enableNetwork(
            mWifiConfiguration.get(index).networkId,
            true
        )
    }

    /**
     * 通过ssid获取Configratin
     */
    fun getConfiguration(ssid: String): Boolean {
        for (configuration in mWifiConfiguration) {
            if (ssid.equals(configuration.SSID.replace("\"", ""))) {
                // 连接配置好的指定ID的网络
                mWifiManager.enableNetwork(
                    configuration.networkId,
                    true
                )
                return true
            }
        }
        return false
    }

    /**
     * 通过ssid获取Configratin netid
     */
    fun getConfigurationByBssid(bssid: String): Int {
        for (configuration in mWifiConfiguration) {
            if (bssid.equals(configuration.BSSID)) {
                return configuration.networkId
            }
        }
        return -1
    }

    /**
     * 开始扫描
     */
    fun startScan() {
        mWifiManager.startScan()
        //得到扫描结果
        mWifiList = mWifiManager.scanResults
        mWifiConfiguration = mWifiManager.configuredNetworks
    }

    /**
     * 得到网络列表
     */
    fun getWifiList(): List<ScanResult> {
        var wifiList = ArrayList<ScanResult>()
        for (wifi in mWifiList) {
            // 该热点SSID是否已在列表中
            var position = getItemPosition(wifiList, wifi)
            if (position != -1) { // 已在列表
                // 相同SSID热点，取信号强的
                if (wifiList.get(position).level < wifi.level) {
                    wifiList.removeAt(position)
                    wifiList.add(position, wifi)
                }
            } else {
                wifiList.add(wifi)

            }
        }
        return wifiList
    }

    /**
     * 返回item在list中的坐标
     */
    private fun getItemPosition(list: List<ScanResult>, item: ScanResult): Int {
        for (i in 0 until list.size) {
            if (item.SSID.equals(list.get(i).SSID)) {
                return i
            }
        }
        return -1
    }

    /**
     * 查看扫描结果
     */
    fun loolUpScan(): java.lang.StringBuilder {
        val stringBuilder = StringBuilder()
        for (i in 0 until mWifiList.size) {
            stringBuilder
                .append("Index_" + (i + 1).toString() + ":")
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            stringBuilder.append(mWifiList[i].toString())
            stringBuilder.append("/n")
        }
        return stringBuilder
    }

    /**
     * 得到MAC地址
     */
    fun getMacAddress(): String {
        return if (mWifiInfo == null) "NULL" else mWifiInfo.getMacAddress()
    }

    /**
     * 得到接入点的BSSID
     */
    fun getBSSID(): String {
        return if (mWifiInfo == null) "NULL" else mWifiInfo.getBSSID()
    }

    fun getSSID(): String {
        return if (mWifiInfo == null) "NULL" else mWifiInfo.ssid
    }

    /**
     * 得到IP地址
     */
    fun getIPAddress(): Int {
        return if (mWifiInfo == null) 0 else mWifiInfo.getIpAddress()
    }

    /**
     * 得到连接的ID
     */
    fun getNetworkId(): Int {
        return if (mWifiInfo == null) 0 else mWifiInfo.getNetworkId()
    }

    /**
     * 得到WifiInfo的所有信息包
     */
    fun getWifiInfo(): String {
        return if (mWifiInfo == null) "NULL" else mWifiInfo.toString()
    }

    /**
     * 添加一个网络并连接
     */
    fun addNetwork(wcg: WifiConfiguration): Boolean {
        var wcgID = mWifiManager.addNetwork(wcg)
        var b = mWifiManager.enableNetwork(wcgID, true)
        System.out.println("a--" + wcgID)
        System.out.println("b--" + b)
        return b
    }

    // 断开指定ID的网络
    fun disconnectWifi(netId: Int) {
        Log.e("Error", "-------------" + netId)
        mWifiManager.disableNetwork(netId)
        mWifiManager.disconnect()
    }

//然后是一个实际应用方法，只验证过没有密码的情况：

    fun CreateWifiInfo(SSID: String, Password: String, Type: Int): WifiConfiguration {
        var config = WifiConfiguration()
        config.allowedAuthAlgorithms.clear()
        config.allowedGroupCiphers.clear()
        config.allowedKeyManagement.clear()
        config.allowedPairwiseCiphers.clear()
        config.allowedProtocols.clear()
        config.SSID = "/" + SSID + "/"

        var tempConfig = IsExsits(SSID)
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId)
        }

        if (Type == 1) //WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = ""
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            config.wepTxKeyIndex = 0
        }
        if (Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true
            config.wepKeys[0] = "/" + Password + "/"
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) //WIFICIPHER_WPA
        {
            config.preSharedKey = "/" + Password + "/"
            config.hiddenSSID = true
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            config.status = WifiConfiguration.Status.ENABLED
        }
        return config
    }

    fun getCipherType(ssid: String): Int {
        var list = mWifiManager.getScanResults()
        for (scResult in list) {
            if (!TextUtils.isEmpty(scResult.SSID)
                && (scResult.SSID.trim().equals(
                    "\"" + ssid + "\"", true
                ) || scResult.SSID.trim()
                    .equals(ssid, true))
            ) {
                var capabilities = scResult.capabilities
                if (!TextUtils.isEmpty(capabilities)) {
                    if (capabilities.contains("WPA")
                        || capabilities.contains("wpa")
                    ) {
                        return 3
                    } else if (capabilities.contains("WEP")
                        || capabilities.contains("wep")
                    ) {
                        return 2
                    } else {
                        return 1
                    }
                }
            }
        }
        return -1
    }

    private fun IsExsits(SSID: String): WifiConfiguration? {
        var existingConfigs = mWifiManager.getConfiguredNetworks()
        for (existingConfig in existingConfigs) {
            if (existingConfig.SSID.equals("/" + SSID + "/")) {
                return existingConfig
            }
        }
        return null
    }

}
