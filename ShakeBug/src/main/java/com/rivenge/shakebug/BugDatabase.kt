package com.rivenge.shakebug

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BugReportEntity::class], version = 1, exportSchema = false)
abstract class BugDatabase : RoomDatabase() {
    abstract fun bugDao(): BugDao

    companion object {
        @Volatile
        private var INSTANCE: BugDatabase? = null

        fun getDatabase(context: Context): BugDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BugDatabase::class.java,
                    "shake_bug_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}