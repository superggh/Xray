package com.xtls.xray

import android.content.Context
import android.content.SharedPreferences
import java.io.File

object Settings {
    var socksAddress: String = "127.0.0.1"
    var socksPort: String = "10808"
    var primaryDns: String = "8.8.8.8"
    var secondaryDns: String = "8.8.4.4"
    var useXray: Boolean = true

    fun xrayConfig(context: Context): File = File(context.filesDir, "config.json")
    fun sharedPref(context: Context): SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
}
