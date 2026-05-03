package com.smsblaster.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "failed_table")
data class FailedSms(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "reason")
    val reason: String = "",

    @ColumnInfo(name = "date")
    val date: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "retry_count")
    var retryCount: Int = 0
)
