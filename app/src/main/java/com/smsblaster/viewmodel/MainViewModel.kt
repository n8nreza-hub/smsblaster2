package com.smsblaster.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.smsblaster.db.entity.Contact
import com.smsblaster.repository.SmsRepository
import com.smsblaster.util.AppPrefs
import com.smsblaster.util.ExcelReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmsRepository(application)
    val prefs = AppPrefs(application)

    val totalContacts: LiveData<Int> = repository.getTotalLive()
    val sentCount: LiveData<Int> = repository.getSentCountLive()
    val progress = repository.getProgressLive()

    private val _importStatus = MutableLiveData<String>()
    val importStatus: LiveData<String> = _importStatus

    private val _isImporting = MutableLiveData(false)
    val isImporting: LiveData<Boolean> = _isImporting

    /**
     * خواندن فایل اکسل و ذخیره در دیتابیس
     */
    fun importExcel(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isImporting.postValue(true)
            _importStatus.postValue("در حال خواندن فایل اکسل...")

            try {
                val phones = ExcelReader.readPhoneNumbers(getApplication(), uri)

                if (phones.isEmpty()) {
                    _importStatus.postValue("هیچ شماره‌ای در فایل پیدا نشد!")
                    return@launch
                }

                // پاک کردن داده‌های قبلی
                repository.clearContacts()
                repository.clearProgress()

                // ذخیره شماره‌های جدید
                val contacts = phones.map { phone ->
                    Contact(phoneNumber = phone, status = Contact.STATUS_PENDING)
                }
                repository.insertContacts(contacts)

                _importStatus.postValue("${phones.size} شماره با موفقیت وارد شد ✅")

            } catch (e: Exception) {
                _importStatus.postValue("خطا در خواندن فایل: ${e.message}")
            } finally {
                _isImporting.postValue(false)
            }
        }
    }

    fun resetProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearProgress()
            repository.clearContacts()
            _importStatus.postValue("پیشرفت ریست شد")
        }
    }
}
