package com.example.phasig

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import android.os.Handler
import android.os.Message
import android.os.HandlerThread
import android.os.Process
import android.widget.Toast
import android.os.Vibrator
import android.os.VibrationEffect;
//import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.lang.Math.sqrt

class MyService : Service(), SensorEventListener {
    private var mSensorManager : SensorManager ?= null
    private var mAccelerometer : Sensor ?= null
    //private var serviceLooper: Looper? = null
    //private var serviceHandler: ServiceHandler? = null
    public var threshold : Double = 0.0;

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
                    VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)

                // it is safe to cancel other vibrations currently taking place
                vibrator.cancel()
                vibrator.vibrate(vibrationEffect1)
            }
        }
    }

    // Handler that receives messages from the thread
    /*private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                //Thread.sleep(3000)

                val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator;
                val vibrationEffect1: VibrationEffect;
                vibrationEffect1 = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE);

                // it is safe to cancel other vibrations currently taking place
                vibrator.cancel();
                vibrator.vibrate(vibrationEffect1);

                Thread.sleep(3000)

            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1)
        }
    }*/

    override fun onDestroy()
    {
        mSensorManager!!.unregisterListener(this)
        //super.onDestroy()
    }

    override fun onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        /*HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            //serviceLooper = looper
            //serviceHandler = ServiceHandler(looper)
        }*/
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()

        if (intent.getAction().equals("apply"))
        {
            threshold = intent.getExtras()!!.getDouble("threshold")
        }

        mSensorManager!!.registerListener(this,mAccelerometer,
            SensorManager.SENSOR_DELAY_GAME)

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        /*serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }*/

        // If we get killed, after returning from here, restart
        return START_REDELIVER_INTENT //START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}