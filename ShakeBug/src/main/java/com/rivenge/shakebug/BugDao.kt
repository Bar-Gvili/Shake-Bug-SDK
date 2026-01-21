package com.rivenge.shakebug

import androidx.room.*

@Dao
interface BugDao {
    @Insert
    suspend fun insertReport(report: BugReportEntity)

    @Query("SELECT * FROM bug_reports ORDER BY timestamp DESC")
    suspend fun getAllReports(): List<BugReportEntity>

    @Delete
    suspend fun deleteReport(report: BugReportEntity)
}