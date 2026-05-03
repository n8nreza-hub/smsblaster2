package com.smsblaster.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.smsblaster.db.AppDatabase
import com.smsblaster.db.entity.Contact
import com.smsblaster.db.entity.FailedSms
import com.smsblaster.db.entity.Progress

class SmsRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val contactDao = db.contactDao()
    private val progressDao = db.progressDao()
    private val failedSmsDao = db.failedSmsDao()

    // ========== contacts ==========

    suspend fun insertContacts(contacts: List<Contact>) = contactDao.insertAll(contacts)

    suspend fun updateContactStatus(id: Long, status: String, simSlot: Int) =
        contactDao.updateStatus(id, status, simSlot)

    suspend fun getPendingContacts() = contactDao.getPending()

    suspend fun getPendingBlock(limit: Int, offset: Int) =
        contactDao.getPendingBlock(limit, offset)

    suspend fun getTotalContacts() = contactDao.getTotal()

    suspend fun getSentCount() = contactDao.getSentCount()

    suspend fun getFailedCount() = contactDao.getFailedCount()

    suspend fun getPendingCount() = contactDao.getPendingCount()

    suspend fun getSim1SentCount() = contactDao.getSim1SentCount()

    suspend fun getSim2SentCount() = contactDao.getSim2SentCount()

    suspend fun clearContacts() = contactDao.deleteAll()

    suspend fun findByPhone(phone: String) = contactDao.findByPhone(phone)

    fun getAllContactsLive(): LiveData<List<Contact>> = contactDao.getAllLive()

    fun getTotalLive(): LiveData<Int> = contactDao.getTotalLive()

    fun getSentCountLive(): LiveData<Int> = contactDao.getSentCountLive()

    // ========== progress ==========

    suspend fun getProgress(): Progress? = progressDao.get()

    suspend fun saveProgress(progress: Progress) = progressDao.insert(progress)

    suspend fun updateProgressIndex(index: Int, totalSent: Int) =
        progressDao.updateIndex(index, totalSent)

    suspend fun setRunning(running: Boolean) = progressDao.setRunning(running)

    suspend fun markCompleted() = progressDao.markCompleted()

    suspend fun clearProgress() = progressDao.deleteAll()

    fun getProgressLive(): LiveData<Progress?> = progressDao.getLive()

    // ========== failed ==========

    suspend fun insertFailed(failedSms: FailedSms) = failedSmsDao.insert(failedSms)

    suspend fun deleteFailed(failedSms: FailedSms) = failedSmsDao.delete(failedSms)

    suspend fun deleteFailedById(id: Long) = failedSmsDao.deleteById(id)

    suspend fun getAllFailed() = failedSmsDao.getAll()

    fun getAllFailedLive(): LiveData<List<FailedSms>> = failedSmsDao.getAllLive()

    suspend fun getFailedTotal() = failedSmsDao.getCount()
}
