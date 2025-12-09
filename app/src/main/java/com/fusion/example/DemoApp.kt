package com.fusion.example

import android.app.Application
import android.util.Log
import com.fusion.adapter.Fusion
import com.fusion.adapter.initialize

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Fusion.initialize {
            setDebug(BuildConfig.DEBUG)
            setErrorListener { item, e ->
                Log.e("Fusion", "Unknown type: " + item.javaClass, e);
            }
        }
    }

}