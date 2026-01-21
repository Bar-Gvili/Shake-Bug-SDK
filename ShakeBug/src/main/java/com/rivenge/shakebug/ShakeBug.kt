package com.rivenge.shakebug

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

object ShakeBug {
    private var isInitialized = false
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var shakeDetector: ShakeDetector? = null
    private var appContext: Context? = null

    fun init(context: Context, developerId: String, appId: String) {
        if (isInitialized) return

        // --- תיקון 1: שמירת ה-Context שקיבלנו מהמפתח ---
        this.appContext = context.applicationContext

        // שמירת המזהים ב-SharedPreferences
        val prefs = context.getSharedPreferences("ShakeBugPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("developer_id", developerId)
            putString("app_id", appId)
            apply()
        }

        // מעקב אחר מחזור החיים של ה-Activities
        val app = context.applicationContext as Application
        app.registerActivityLifecycleCallbacks(ActivityLifecycleTracker)

        // אתחול חיישני התנועה
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // הגדרת זיהוי ה"שקשוק"
        shakeDetector = ShakeDetector {
            handleShake()
        }

        startListening()
        isInitialized = true
        Log.d("ShakeBug", "SDK Initialized for Dev: $developerId, App: $appId")
    }

    private fun startListening() {
        sensorManager?.registerListener(
            shakeDetector,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    private fun handleShake() {
        val currentActivity = ActivityLifecycleTracker.getCurrentActivity() ?: return

        // הפעלת ה-Activity של דיווח הבאג
        val intent = Intent(currentActivity, BugReportActivity::class.java)
        currentActivity.startActivity(intent)
    }

    fun stop() {
        sensorManager?.unregisterListener(shakeDetector)
        Log.d("ShakeBug", "SDK Stopped listening")
    }
}