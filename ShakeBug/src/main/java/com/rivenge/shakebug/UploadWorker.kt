package com.rivenge.shakebug

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.time.Instant

class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        println("DEBUG: UploadWorker started")
        val database = BugDatabase.getDatabase(applicationContext)
        val repo = database.bugDao()
        val pendingReports = repo.getAllReports()

        println("DEBUG: Found ${pendingReports.size} pending reports")

        if (pendingReports.isEmpty()) {
            return Result.success()
        }

        val prefs = applicationContext.getSharedPreferences("ShakeBugPrefs", Context.MODE_PRIVATE)
        val devId = prefs.getString("developer_id", null)
        val appId = prefs.getString("app_id", null) // --- התיקון --- Get the correct App ID

        if (devId == null || appId == null) {
            println("ERROR: devId or appId is null, cannot upload.")
            return Result.failure()
        }
        println("DEBUG: devId: $devId, appId: $appId")

        val apiKey = "AIzaSyC4wHTC5WcNmGqM0tw-DmiKpcwo233jac0"
        val projectId = "shakebug-platform"

        val client = OkHttpClient()

        for (report in pendingReports) {
            try {
                val downloadUrl = "" // Screenshot upload is disabled

                val fields = JSONObject().apply {
                    put("description", JSONObject().put("stringValue", report.description))
                    put("deviceInfo", JSONObject().put("stringValue", report.deviceMetadata))
                    put("screenshotUrl", JSONObject().put("stringValue", downloadUrl))
                    val timestampStr = Instant.ofEpochMilli(report.timestamp).toString()
                    put("timestamp", JSONObject().put("timestampValue", timestampStr))
                    put("appPackage", JSONObject().put("stringValue", applicationContext.packageName))
                }
                val jsonPayload = JSONObject().put("fields", fields)

                // --- התיקון --- Use appId in the URL instead of packageName
                val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/developers/$devId/apps/$appId/reports?key=$apiKey"
                val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() // Read the body once

                if (response.isSuccessful) {
                    println("SUCCESS: Report sent via REST API. Response: $responseBody")
                    
                    val localFile = File(report.imagePath)
                    if (localFile.exists()) {
                        localFile.delete()
                    }
                    repo.deleteReport(report)

                } else {
                    println("ERROR: REST API Server error: ${response.code}. Response: $responseBody")
                    return Result.retry()
                }

            } catch (e: Exception) {
                println("EXCEPTION during REST API upload: ${e.message}")
                return Result.retry()
            }
        }

        return Result.success()
    }
}
