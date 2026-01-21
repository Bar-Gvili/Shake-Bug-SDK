package com.rivenge.shakebug

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class BugReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bug_report) // ודא שה-XML תואם

        // חיבור האלמנטים מה-XML
        val imageView = findViewById<ImageView>(R.id.bugImageView) // הוסף ImageView ל-XML אם עדיין אין
        val deviceInfoText = findViewById<TextView>(R.id.deviceInfoText)
        val descriptionInput = findViewById<EditText>(R.id.bugDescriptionEntry)
        val sendBtn = findViewById<Button>(R.id.sendReportBtn)

        // 1. הצגת צילום המסך שקיבלנו מה-ShakeBug
        val imagePath = intent.getStringExtra("SCREENSHOT_PATH")
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            imageView.setImageBitmap(bitmap)
        }

        // 2. איסוף נתוני מכשיר ואפליקציה אוטומטיים
        val fullInfo = collectMetadata()
        deviceInfoText.text = fullInfo

        // 3. לוגיקה לכפתור השליחה
        sendBtn.setOnClickListener {
            val description = descriptionInput.text.toString()
            val deviceInfo = deviceInfoText.text.toString()

            if (description.isBlank()) {
                Toast.makeText(this, "אנא הזן תיאור לבאג", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // יצירת אובייקט הדיווח
            val report = BugReportEntity(
                description = description,
                deviceMetadata = deviceInfo,
                imagePath = imagePath ?: ""
            )

            // שמירה בבסיס הנתונים בתוך Coroutine
            lifecycleScope.launch(Dispatchers.IO) {
                val db = BugDatabase.getDatabase(applicationContext)
                db.bugDao().insertReport(report)

                // --- הוספת הקוד החסר להפעלת ה-WorkManager ---
                val constraints = androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()

                val uploadWorkRequest = androidx.work.OneTimeWorkRequestBuilder<UploadWorker>()
                    .setConstraints(constraints)
                    .build()

                androidx.work.WorkManager.getInstance(applicationContext).enqueue(uploadWorkRequest)
                // --------------------------------------------

                // חזרה ל-Main Thread להצגת הודעה וסגירת המסך
                launch(Dispatchers.Main) {
                    Toast.makeText(this@BugReportActivity, "הדיווח נשמר! הוא יישלח ברגע שיהיה חיבור לאינטרנט.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    /**
     * פונקציה שאוספת מידע טכני על המכשיר והאפליקציה
     */
    private fun collectMetadata(): String {
        val packageName = packageName
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val appVersion = packageInfo.versionName
        val appBuild = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toString()
        } else {
            packageInfo.versionCode.toString()
        }

        return """
            --- App Info ---
            App: $packageName
            Version: $appVersion ($appBuild)
            
            --- Device Info ---
            Brand: ${Build.MANUFACTURER}
            Model: ${Build.MODEL}
            Android Version: ${Build.VERSION.RELEASE}
            SDK Level: ${Build.VERSION.SDK_INT}
        """.trimIndent()
    }
}