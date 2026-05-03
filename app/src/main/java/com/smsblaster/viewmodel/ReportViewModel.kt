package com.smsblaster.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.smsblaster.repository.SmsRepository
import kotlinx.coroutines.launch

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmsRepository(application)

    val progress = repository.getProgressLive()

    private val _sim1Count = MutableLiveData(0)
    val sim1Count: LiveData<Int> = _sim1Count

    private val _sim2Count = MutableLiveData(0)
    val sim2Count: LiveData<Int> = _sim2Count

    private val _failedCount = MutableLiveData(0)
    val failedCount: LiveData<Int> = _failedCount

    private val _totalCount = MutableLiveData(0)
    val totalCount: LiveData<Int> = _totalCount

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _sim1Count.postValue(repository.getSim1SentCount())
            _sim2Count.postValue(repository.getSim2SentCount())
            _failedCount.postValue(repository.getFailedCount())
            _totalCount.postValue(repository.getTotalContacts())
        }
    }
}
