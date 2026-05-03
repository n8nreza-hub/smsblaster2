package com.smsblaster.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress_table")
data class Progress(
    @PrimaryKey
    val id: Int = 1, // همیشه یک ردیف داریم

    @ColumnInfo(name = "last_sent_index")
    var lastSentIndex: Int = 0,

    @ColumnInfo(name = "current_block")
    var currentBlock: Int = 0,

    @ColumnInfo(name = "sim1_count")
    var sim1Count: Int = 0,

    @ColumnInfo(name = "sim2_count")
    var sim2Count: Int = 0,

    @ColumnInfo(name = "total_sent")
    var totalSent: Int = 0,

    @ColumnInfo(name = "file_completed")
    var fileCompleted: Boolean = false,

    @ColumnInfo(name = "is_running")
    var isRunning: Boolean = false,

    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = System.currentTimeMillis()
)
