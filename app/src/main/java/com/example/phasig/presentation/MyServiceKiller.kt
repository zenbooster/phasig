package com.example.phasig.presentation

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

class MyServiceKiller : Service() {
    var victim : Intent? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.getAction().equals("apply"))
        {
            victim = intent.getExtras()!!.get("victim") as Intent?
            Core.btnChecked.value = true

            stopService(victim)
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}