package com.joe.wifiutils.ui.main

import android.Manifest
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.joe.wifiutils.R
import com.joe.wifiutils.base.BaseActivity
import com.joe.wifiutils.reciver.WifiBroadcastReceiver
import com.joe.wifiutils.ui.wifidetail.WifiActivity
import com.joe.wifiutils.utils.WifiControlUtils
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionListener
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.layout_dialog_editpsd.view.*
import kotlinx.android.synthetic.main.layout_recycle_item.view.*


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    override val layoutId: Int = R.layout.activity_main

    private lateinit var wifiBroadcastReceiver: WifiBroadcastReceiver
    private lateinit var scanResults: List<ScanResult>
    private lateinit var wifiUtils: WifiControlUtils

    override fun init() {
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPerssion()
        }

        //动态注册广播
        wifiBroadcastReceiver = WifiBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        registerReceiver(wifiBroadcastReceiver, intentFilter)

        val fab: FloatingActionButton = findViewById(com.joe.wifiutils.R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(com.joe.wifiutils.R.id.drawer_layout)
        val navView: NavigationView = findViewById(com.joe.wifiutils.R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        //开启wifi
        wifiUtils = WifiControlUtils.get(applicationContext)
        wifiUtils.openWifi()
        wifiUtils.startScan()
        scanResults = wifiUtils.getWifiList()


        recycleview_main.layoutManager = LinearLayoutManager(applicationContext)
        recycleview_main.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
        recycleview_main.adapter = object : RecyclerView.Adapter<MyViewHolder>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
                var vh = MyViewHolder(layoutInflater.inflate(R.layout.layout_recycle_item, p0, false))
                return vh
            }

            override fun getItemCount(): Int {
                return scanResults.size
            }

            override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
                p0.textView.text = scanResults.get(p1).BSSID
                p0.name.text = scanResults.get(p1).SSID
                p0.ssid.text = "" + scanResults.get(p1).level
                p0.view.setOnLongClickListener {
                    //弹出对话框
                    showDialogs(scanResults[p1])
                    true
                }
            }

        }
    }


    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(com.joe.wifiutils.R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_tools -> {
                jumpToActivity(WifiActivity::class.java)
            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }
        val drawerLayout: DrawerLayout = findViewById(com.joe.wifiutils.R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun requestPerssion() {
        AndPermission.with(this)
            .requestCode(100)
            .permission(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .callback(object : PermissionListener {
                override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {

                }

                override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {
                    if (requestCode == 100) {
                        finish()
                    }
                }

            })
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiBroadcastReceiver)
    }


    /**
     * 普通dialog
     */
    fun showDialogs(scanResult: ScanResult) {
        val normalDialog =
            AlertDialog.Builder(this)
        //normalDialog.setIcon(R.drawable.ic_launcher_background)
        normalDialog.setTitle("Connect Wifi")
        normalDialog.setMessage(scanResult.SSID)
        normalDialog.setNegativeButton("Cancel", null)
        normalDialog.setPositiveButton("Connect", { dialog, which ->
            //判断设备是否已经连接wifi
            if (wifiUtils.getWifiInfo() != null && !wifiUtils.getSSID().replace("\"","").equals(scanResult.SSID,true) ) {
                //先断开连接
                //wifiUtils.disconnectWifi(wifiUtils.getNetworkId())

                //判断是否要输入密码
                var conf = wifiUtils.getConfiguration(scanResult.SSID)
                if (!conf) {
                    //创建configuration
                    var i = wifiUtils.getCipherType(scanResult.SSID)
                    if (i == 1) {
                        //创建config
                        var conf =
                            wifiUtils.CreateWifiInfo(scanResult.SSID, "", wifiUtils.getCipherType(scanResult.SSID))
                        //连接wifi
                        wifiUtils.addNetwork(conf)
                    } else {
                        createCongAndCont(scanResult)
                    }
                }
            } else if (wifiUtils.getWifiInfo() == null) {
                //判断是否要输入密码
                var conf = wifiUtils.getConfiguration(scanResult.SSID)
                if (!conf) {
                    //创建configuration
                    var i = wifiUtils.getCipherType(scanResult.SSID)
                    if (i == 1) {
                        //创建config
                        var conf =
                            wifiUtils.CreateWifiInfo(scanResult.SSID, "", wifiUtils.getCipherType(scanResult.SSID))
                        //连接wifi
                        wifiUtils.addNetwork(conf)
                    } else {
                        createCongAndCont(scanResult)
                    }
                }
            }
        })
        // 显示
        normalDialog.show()
    }

    //创建新连接
    fun createCongAndCont(scanResult: ScanResult) {
        //弹出对话框输入密码
        val normalDialog =
            AlertDialog.Builder(this)
        //normalDialog.setIcon(R.drawable.ic_launcher_background)
        normalDialog.setTitle("Connect Wifi")
        var v = layoutInflater.inflate(R.layout.layout_dialog_editpsd, null)
        normalDialog.setView(v)
        normalDialog.setNegativeButton("Cancel", null)
        normalDialog.setPositiveButton("Connect", { dialog, which ->
            //创建config
            var conf = wifiUtils.CreateWifiInfo(
                scanResult.SSID,
                v.dialog_psd.text.toString().trim(),
                wifiUtils.getCipherType(scanResult.SSID)
            )
            //连接wifi
            var b = wifiUtils.addNetwork(conf)
            if(b){
                Toast.makeText(applicationContext,"连接成功！",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(applicationContext,"连接失败！",Toast.LENGTH_SHORT).show()
            }
        })
        normalDialog.setCancelable(false)
        // 显示
        normalDialog.show()
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var view = view
        var textView: TextView = view.recycle_item_tv
        var name: TextView = view.recycle_item_name
        var ssid: TextView = view.recycle_item_ssid
    }

}
