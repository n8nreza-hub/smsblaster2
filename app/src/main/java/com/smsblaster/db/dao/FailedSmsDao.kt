package com.smsblaster.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.smsblaster.db.entity.FailedSms

@Dao
interface FailedSmsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(failedSms: FailedSms)

    @Update
    suspend fun update(failedSms: FailedSms)

    @Delete
    suspend fun delete(failedSms: FailedSms)

    @Query("SELECT * FROM failed_table ORDER BY date DESC")
    fun getAllLive(): LiveData<List<FailedSms>>

    @Query("SELECT * FROM failed_table ORDER BY date DESC")
    suspend fun getAll(): List<FailedSms>

    @Query("SELECT COUNT(*) FROM failed_table")
    suspend fun getCount(): Int

    @Query("DELETE FROM failed_table WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM failed_table")
    suspend fun deleteAll()
}
