package com.example.phasig.presentation

import android.app.AlarmManager
import android.app.Service
import android.app.Notification
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import android.os.IBinder
import android.os.Build
import android.widget.Toast
import android.os.Vibrator
import android.os.VibrationEffect;
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.lang.Math.sqrt
import android.graphics.Color
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.os.PowerManager
import com.example.phasig.R

class MyService : Service(), SensorEventListener {
    private var mSensorManager : SensorManager ?= null
    private var mAccelerometer : Sensor ?= null

    public var threshold : Double = 0.0;

    var wakeLock: PowerManager.WakeLock? = null

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int)
    {
    }

    override fun onSensorChanged(event: SensorEvent?)
    {
        if (event != null) {
            //ground!!.updateMe(event.values[1] , event.values[0])

            val arr = event.values
            val squareSum = arr.map { n: Float -> (n.toDouble() * n.toDouble()) }.sum()
            val v = sqrt(squareSum)

            if (v > threshold)
            {
                val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                val vibrationEffect1: VibrationEffect
                vibrationEffect1 =
                    VibrationEffect.createOneShot(Core.islrVibrationDuration.value.toLong(), Core.islrVibrationLevel.value.toInt())

                // it is safe to cancel other vibrations currently taking place
                vibrator.cancel()
                vibrator.vibrate(vibrationEffect1)
            }
        }
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "Endless Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Endless Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            putExtra("type", type)
        }
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            notificationChannelId
        ) else Notification.Builder(this)

        return builder
            .setContentTitle("Phasyg service")
            .setContentText("Hunting for awakenings...")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    override fun onDestroy()
    {
        mSensorManager!!.unregisterListener(this)
        wakeLock!!.release()
        Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate() {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        threshold = Core.pkrItems[Core.pkrState.selectedOption].toDouble()

        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "phasig::MyWakelockTag").apply {
                    acquire()
                }
            }

        mSensorManager!!.registerListener(this,mAccelerometer,
            SensorManager.SENSOR_DELAY_GAME)

        Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show()
        // If we get killed, after returning from here, restart
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}