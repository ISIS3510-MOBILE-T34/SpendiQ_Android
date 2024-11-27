package com.isis3510.spendiq.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import android.content.Context
import android.content.Intent
import android.util.Log
import com.isis3510.spendiq.services.DataFetchService

class AppLifecycleObserver(private val context: Context) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d("AppLifecycleObserver", "App went to background, starting service")
        startFetchService()
    }

    private fun startFetchService() {
        val serviceIntent = Intent(context, DataFetchService::class.java)
        context.startService(serviceIntent)
    }
}

