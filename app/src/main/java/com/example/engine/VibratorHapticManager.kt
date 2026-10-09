package com.example.engine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi

object VibratorHapticManager {

    @RequiresApi(Build.VERSION_CODES.O)
    fun click(context: Context) {
        Log.d("GridMemory", "Button đã được nhấn!")
        vibrate(context, 25L, 60)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun success(context: Context) {
        vibrate(context, 40L, 100)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun error(context: Context) {
        vibrate(context, 80L, 150)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun vibrate(
        context: Context,
        duration: Long,
        amplitude: Int
    ) {
        val vibrator = context.getSystemService(Vibrator::class.java)

        vibrator?.vibrate(
            VibrationEffect.createOneShot(
                duration,
                amplitude
            )
        )
    }
}