package com.rivenge.shakebug

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream

object ScreenshotUtils {

    /**
     * פונקציה שמקבלת Activity ומחזירה צילום מסך כ-Bitmap
     */
    fun capture(activity: Activity, callback: (Bitmap?) -> Unit) {
        val view = activity.window.decorView.rootView

        // באנדרואיד 8 ומעלה, מומלץ להשתמש ב-PixelCopy כי הוא מטפל טוב יותר ב-Layers
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bitmap = createBitmap(view.width, view.height)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)

            try {
                PixelCopy.request(
                    activity.window,
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            callback(bitmap)
                        } else {
                            callback(captureOldWay(view)) // גיבוי לשיטה הישנה
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
            } catch (e: Exception) {
                callback(captureOldWay(view))
            }
        } else {
            // למכשירים ישנים מאוד
            callback(captureOldWay(view))
        }
    }

    // השיטה הקלאסית (לפעמים פחות טובה עם SurfaceViews או וידאו)
    private fun captureOldWay(view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    // בתוך ScreenshotUtils.kt - פונקציה לשמירה קבועה
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? {
        return try {
            val folder = File(context.filesDir, "pending_reports")
            if (!folder.exists()) folder.mkdirs()

            val file = File(folder, "report_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap): String? {
        return try {
            val cachePath = File(context.cacheDir, "screenshots")
            cachePath.mkdirs() // יצירת התיקייה אם אינה קיימת
            val fileName = "bug_report_${System.currentTimeMillis()}.png"
            val file = File(cachePath, fileName)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            file.absolutePath // החזרת הנתיב המלא לקובץ
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}