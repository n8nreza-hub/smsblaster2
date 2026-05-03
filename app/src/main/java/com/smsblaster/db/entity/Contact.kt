package com.smsblaster.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts_table")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "status")
    var status: String = STATUS_PENDING, // pending / sent / failed

    @ColumnInfo(name = "sim_slot")
    var simSlot: Int = 0, // 0 = هنوز تعیین نشده، 1 = سیم 1، 2 = سیم 2

    @ColumnInfo(name = "block_number")
    var blockNumber: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_SENT = "sent"
        const val STATUS_FAILED = "failed"
    }
}
