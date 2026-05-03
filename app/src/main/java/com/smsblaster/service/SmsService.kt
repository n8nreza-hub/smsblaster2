package com.smsblaster.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import com.smsblaster.R
import com.smsblaster.db.entity.Contact
import com.smsblaster.db.entity.FailedSms
import com.smsblaster.db.entity.Progress
import com.smsblaster.repository.SmsRepository
import com.smsblaster.ui.main.MainActivity
import com.smsblaster.util.AppPrefs
import com.smsblaster.util.TimeChecker
import kotlinx.coroutines.*

class SmsService : Service() {

    companion object {
        const val CHANNEL_ID = "sms_service_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        // برای ارتباط با UI
        var isRunning = false
        var currentStatus = ""
        var onStatusUpdate: ((String) -> Unit)? = null
    }

    private lateinit var repository: SmsRepository
    private lateinit var prefs: AppPrefs
    private var serviceJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        repository = SmsRepository(applicationContext)
        prefs = AppPrefs(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startSending()
            ACTION_STOP -> stopSending()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startSending() {
        isRunning = true
        prefs.isSending = true
        startForeground(NOTIFICATION_ID, buildNotification("در حال آماده‌سازی..."))

        serviceJob = serviceScope.launch {
            try {
                sendSmsLoop()
            } catch (e: Exception) {
                e.printStackTrace()
                updateStatus("خطا: ${e.message}")
            } finally {
                isRunning = false
                prefs.isSending = false
                repository.setRunning(false)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun stopSending() {
        isRunning = false
        prefs.isSending = false
        serviceJob?.cancel()
        serviceScope.launch {
            repository.setRunning(false)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * حلقه اصلی ارسال پیامک
     * منطق: سیم 1 → بلوک کامل → سیم 2 → بلوک کامل → تکرار
     */
    private suspend fun sendSmsLoop() {
        val blockSize = prefs.blockSize
        val message = prefs.smsMessage
        val sim1SubId = prefs.sim1SubscriptionId
        val sim2SubId = prefs.sim2SubscriptionId
        val delayMs = prefs.sendDelaySeconds * 1000L

        if (message.isEmpty()) {
            updateStatus("متن پیامک تنظیم نشده!")
            return
        }

        // گرفتن یا ساخت progress
        var progress = repository.getProgress() ?: Progress(id = 1)
        if (!progress.isRunning) {
            progress = progress.copy(isRunning = true)
            repository.saveProgress(progress)
        }

        val allPending = repository.getPendingContacts()

        if (allPending.isEmpty()) {
            updateStatus("همه پیامک‌ها ارسال شده‌اند ✅")
            repository.markCompleted()
            return
        }

        var currentIndex = 0
        var currentBlock = progress.currentBlock

        while (isRunning && currentIndex < allPending.size) {

            // بررسی بازه زمانی
            if (!TimeChecker.isInAllowedTime(prefs.startHour, prefs.endHour)) {
                updateStatus("خارج از بازه زمانی مجاز - منتظر...")
                delay(60_000) // یک دقیقه صبر
                continue
            }

            // بررسی محدودیت روزانه
            val todayStr = TimeChecker.getTodayString()
            if (prefs.dailyResetDate != todayStr) {
                prefs.dailySentToday = 0
                prefs.dailyResetDate = todayStr
            }

            if (prefs.dailySentToday >= prefs.dailyLimit) {
                updateStatus("سقف روزانه پر شده - فردا ادامه می‌دهم")
                delay(60_000)
                continue
            }

            // تعیین بلوک فعلی
            val blockStart = currentBlock * blockSize * 2 // هر دور = 2 بلوک (سیم 1 + سیم 2)

            // ===== ارسال با سیم 1 =====
            if (sim1SubId != -1) {
                val sim1Start = blockStart
                val sim1End = minOf(sim1Start + blockSize, allPending.size)

                for (i in sim1Start until sim1End) {
                    if (!isRunning) return
                    val contact = allPending[i]
                    sendSingleSms(contact, sim1SubId, 1, message, delayMs)
                    progress.lastSentIndex = i
                    progress.sim1Count++
                    progress.totalSent++
                    prefs.dailySentToday++
                    repository.updateProgressIndex(i, progress.totalSent)
                    updateStatus("سیم ۱ | بلوک ${currentBlock + 1} | ${i + 1}/${allPending.size}")
                }
                currentIndex = sim1End
            }

            // ===== ارسال با سیم 2 =====
            if (sim2SubId != -1 && isRunning) {
                val sim2Start = blockStart + blockSize
                val sim2End = minOf(sim2Start + blockSize, allPending.size)

                for (i in sim2Start until sim2End) {
                    if (!isRunning) return
                    val contact = allPending[i]
                    sendSingleSms(contact, sim2SubId, 2, message, delayMs)
                    progress.lastSentIndex = i
                    progress.sim2Count++
                    progress.totalSent++
                    prefs.dailySentToday++
                    repository.updateProgressIndex(i, progress.totalSent)
                    updateStatus("سیم ۲ | بلوک ${currentBlock + 1} | ${i + 1}/${allPending.size}")
                }
                currentIndex = sim2Start + blockSize
            }

            currentBlock++
            progress.currentBlock = currentBlock
            repository.saveProgress(progress)

            if (currentIndex >= allPending.size) break
        }

        if (!isRunning) {
            updateStatus("ارسال متوقف شد")
        } else {
            updateStatus("ارسال کامل شد ✅")
            repository.markCompleted()
        }
    }

    private suspend fun sendSingleSms(
        contact: Contact,
        subscriptionId: Int,
        simSlot: Int,
        message: String,
        delayMs: Long
    ) {
        try {
            val smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
            val parts = smsManager.divideMessage(message)

            if (parts.size == 1) {
                smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
            } else {
                smsManager.sendMultipartTextMessage(
                    contact.phoneNumber, null, parts, null, null
                )
            }

            repository.updateContactStatus(contact.id, Contact.STATUS_SENT, simSlot)
            delay(delayMs)

        } catch (e: Exception) {
            e.printStackTrace()
            repository.updateContactStatus(contact.id, Contact.STATUS_FAILED, simSlot)
            repository.insertFailed(
                FailedSms(
                    phoneNumber = contact.phoneNumber,
                    reason = e.message ?: "خطای ناشناخته"
                )
            )
        }
    }

    private fun updateStatus(status: String) {
        currentStatus = status
        onStatusUpdate?.invoke(status)
        updateNotification(status)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "سرویس ارسال پیامک",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "نمایش وضعیت ارسال پیامک انبوه"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(status: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, SmsService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("پیامک انبوه در حال ارسال")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "توقف", stopIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(status: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(status))
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceJob?.cancel()
    }
}
