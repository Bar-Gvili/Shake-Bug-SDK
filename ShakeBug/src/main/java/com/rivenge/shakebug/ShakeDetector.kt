package com.rivenge.shakebug

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.lang.Math.sqrt

internal class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    // ערכי סף - ניתן לשנות את הרגישות כאן
    private val threshold = 1.5f // כוח ה-G שמעליו זה נחשב ניעור
    private var lastShakeTime: Long = 0
    private val SHAKE_SLOP_TIME_MS = 500 // זמן המתנה בין ניעור לניעור (חצי שנייה)

    override fun onSensorChanged(event: SensorEvent) {
        // קבלת ערכי X, Y, Z מחיישן התאוצה
       // Log.d("ShakeBug", "Sensor moving: x=${event?.values?.get(0)}")
        val x = event.values[0] / SensorManager.GRAVITY_EARTH
        val y = event.values[1] / SensorManager.GRAVITY_EARTH
        val z = event.values[2] / SensorManager.GRAVITY_EARTH

        // חישוב כוח ה-G הכולל (מרחק וקטורי ממרכז הצירים)
        val gForce = sqrt((x * x + y * y + z * z).toDouble())

        if (gForce > threshold) {
            val now = System.currentTimeMillis()

            // בדיקה שלא זיהינו ניעור ממש הרגע (מניעת קפיצות כפולות)
            if (lastShakeTime + SHAKE_SLOP_TIME_MS > now) {
                return
            }

            lastShakeTime = now
            Log.d("ShakeBug", "Shake Detected!")
            onShake() // הפעלת הפונקציה שקיבלנו ב-Constructor
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // אין צורך למימוש ברוב המקרים
    }
}