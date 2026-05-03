package com.smsblaster.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.smsblaster.db.entity.Contact

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(contacts: List<Contact>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact): Long

    @Update
    suspend fun update(contact: Contact)

    @Query("SELECT * FROM contacts_table ORDER BY id ASC")
    fun getAllLive(): LiveData<List<Contact>>

    @Query("SELECT * FROM contacts_table ORDER BY id ASC")
    suspend fun getAll(): List<Contact>

    @Query("SELECT * FROM contacts_table WHERE status = 'pending' ORDER BY id ASC")
    suspend fun getPending(): List<Contact>

    @Query("SELECT * FROM contacts_table WHERE status = 'pending' ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getPendingBlock(limit: Int, offset: Int): List<Contact>

    @Query("SELECT COUNT(*) FROM contacts_table")
    suspend fun getTotal(): Int

    @Query("SELECT COUNT(*) FROM contacts_table WHERE status = 'sent'")
    suspend fun getSentCount(): Int

    @Query("SELECT COUNT(*) FROM contacts_table WHERE status = 'failed'")
    suspend fun getFailedCount(): Int

    @Query("SELECT COUNT(*) FROM contacts_table WHERE status = 'pending'")
    suspend fun getPendingCount(): Int

    @Query("UPDATE contacts_table SET status = :status, sim_slot = :simSlot WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, simSlot: Int)

    @Query("DELETE FROM contacts_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM contacts_table WHERE phone_number = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): Contact?

    @Query("SELECT COUNT(*) FROM contacts_table WHERE status = 'sent' AND sim_slot = 1")
    suspend fun getSim1SentCount(): Int

    @Query("SELECT COUNT(*) FROM contacts_table WHERE status = 'sent' AND sim_slot = 2")
    suspend fun getSim2SentCount(): Int

    @Query("SELECT COUNT(*) FROM contacts_table")
    fun getTotalLive(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM contacts_table WHERE status = 'sent'")
    fun getSentCountLive(): LiveData<Int>
}
