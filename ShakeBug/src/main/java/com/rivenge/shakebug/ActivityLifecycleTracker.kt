package com.rivenge.shakebug

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

object ActivityLifecycleTracker : Application.ActivityLifecycleCallbacks {

    // שימוש ב-WeakReference כדי למנוע Memory Leaks
    private var currentActivity: WeakReference<Activity>? = null

    fun getCurrentActivity(): Activity? = currentActivity?.get()

    override fun onActivityResumed(activity: Activity) {
        // ברגע ש-Activity עולה לפרונט, אנחנו שומרים אותה
        currentActivity = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        // כשהיא יוצאת מהפוקוס, ננקה את הרפרנס אם זו אותה Activity
        if (currentActivity?.get() == activity) {
            currentActivity = null
        }
    }

    // שאר הפונקציות חייבות מימוש ריק (Empty overrides)
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}