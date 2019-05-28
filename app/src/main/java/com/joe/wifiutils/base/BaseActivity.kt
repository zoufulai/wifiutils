package com.joe.wifiutils.base

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        init()
    }

    abstract val layoutId: Int

    val TAG: String = javaClass.simpleName

    abstract fun init()

    fun jumpToActivity(t: Class<*>) {
        var intent = Intent(this, t)
        startActivity(intent)
    }
}
