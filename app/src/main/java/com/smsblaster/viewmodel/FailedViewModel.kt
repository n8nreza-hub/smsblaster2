package com.smsblaster.viewmodel

import android.app.Application
import android.telephony.SmsManager
import androidx.lifecycle.*
import com.smsblaster.db.entity.FailedSms
import com.smsblaster.repository.SmsRepository
import com.smsblaster.util.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FailedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmsRepository(application)
    private val prefs = AppPrefs(application)

    val failedList: LiveData<List<FailedSms>> = repository.getAllFailedLive()

    private val _retryStatus = MutableLiveData<String>()
    val retryStatus: LiveData<String> = _retryStatus

    fun retrySingle(failedSms: FailedSms) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val subId = prefs.sim1SubscriptionId
                val message = prefs.smsMessage

                if (message.isEmpty()) {
                    _retryStatus.postValue("متن پیامک تنظیم نشده!")
                    return@launch
                }

                val smsManager = SmsManager.getSmsManagerForSubscriptionId(subId)
                smsManager.sendTextMessage(failedSms.phoneNumber, null, message, null, null)

                // حذف از لیست ناموفق
                repository.deleteFailedById(failedSms.id)
                _retryStatus.postValue("${failedSms.phoneNumber} مجدداً ارسال شد ✅")

            } catch (e: Exception) {
                val updated = failedSms.copy(retryCount = failedSms.retryCount + 1)
                repository.insertFailed(updated)
                _retryStatus.postValue("ارسال مجدد ناموفق: ${e.message}")
            }
        }
    }

    fun retryAll() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getAllFailed()
            var successCount = 0

            for (failed in list) {
                try {
                    val subId = prefs.sim1SubscriptionId
                    val message = prefs.smsMessage
                    val smsManager = SmsManager.getSmsManagerForSubscriptionId(subId)
                    smsManager.sendTextMessage(failed.phoneNumber, null, message, null, null)
                    repository.deleteFailedById(failed.id)
                    successCount++
                    Thread.sleep(2000)
                } catch (e: Exception) {
                    val updated = failed.copy(retryCount = failed.retryCount + 1)
                    repository.insertFailed(updated)
                }
            }
            _retryStatus.postValue("$successCount پیامک مجدداً ارسال شد")
        }
    }
}
