package com.fusion.example.app

import android.app.Application
import android.util.Log
import com.fusion.adapter.Fusion
import com.fusion.adapter.initialize
import com.fusion.example.BuildConfig

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        val logDir = externalCacheDir?.absolutePath ?: filesDir.absolutePath
        
        Fusion.initialize {
            setDebug(BuildConfig.DEBUG)
            setLogOutput(BuildConfig.DEBUG, logDir)
            
            setErrorListener { item, e ->
                Log.e("Fusion", "Unknown type: " + item.javaClass, e);
            }
            setGlobalDebounceInterval(300)
            setDefaultItemIdEnabled(true)
        }
    }

}