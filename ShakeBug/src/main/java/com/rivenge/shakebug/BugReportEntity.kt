package com.rivenge.shakebug

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bug_reports")
data class BugReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,               // מזהה ייחודי שנוצר אוטומטית לכל דיווח
    val description: String,       // התיאור שהמשתמש הזין במסך הדיווח
    val deviceMetadata: String,    // נתוני המכשיר והאפליקציה שאספנו
    val imagePath: String,         // הנתיב לקובץ התמונה שנשמר ב-Internal Storage
    val timestamp: Long = System.currentTimeMillis() // זמן יצירת הדיווח
)