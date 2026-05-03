package com.smsblaster.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.smsblaster.db.entity.Progress

@Dao
interface ProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: Progress)

    @Update
    suspend fun update(progress: Progress)

    @Query("SELECT * FROM progress_table WHERE id = 1")
    suspend fun get(): Progress?

    @Query("SELECT * FROM progress_table WHERE id = 1")
    fun getLive(): LiveData<Progress?>

    @Query("UPDATE progress_table SET last_sent_index = :index, total_sent = :totalSent, updated_at = :time WHERE id = 1")
    suspend fun updateIndex(index: Int, totalSent: Int, time: Long = System.currentTimeMillis())

    @Query("UPDATE progress_table SET is_running = :running WHERE id = 1")
    suspend fun setRunning(running: Boolean)

    @Query("UPDATE progress_table SET file_completed = 1 WHERE id = 1")
    suspend fun markCompleted()

    @Query("DELETE FROM progress_table")
    suspend fun deleteAll()
}
