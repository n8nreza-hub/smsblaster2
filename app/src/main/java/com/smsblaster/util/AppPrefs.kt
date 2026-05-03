package com.smsblaster.util

import android.content.Context
import android.content.SharedPreferences

class AppPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sms_blaster_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_BLOCK_SIZE = "block_size"
        const val KEY_DAILY_LIMIT = "daily_limit"
        const val KEY_SEND_DELAY = "send_delay_seconds"
        const val KEY_START_HOUR = "start_hour"
        const val KEY_END_HOUR = "end_hour"
        const val KEY_SMS_MESSAGE = "sms_message"
        const val KEY_SIM1_SUB_ID = "sim1_subscription_id"
        const val KEY_SIM2_SUB_ID = "sim2_subscription_id"
        const val KEY_SIM1_NAME = "sim1_name"
        const val KEY_SIM2_NAME = "sim2_name"
        const val KEY_IS_SENDING = "is_sending"
        const val KEY_DAILY_SENT_TODAY = "daily_sent_today"
        const val KEY_DAILY_RESET_DATE = "daily_reset_date"
    }

    var blockSize: Int
        get() = prefs.getInt(KEY_BLOCK_SIZE, 250)
        set(value) = prefs.edit().putInt(KEY_BLOCK_SIZE, value).apply()

    var dailyLimit: Int
        get() = prefs.getInt(KEY_DAILY_LIMIT, 500)
        set(value) = prefs.edit().putInt(KEY_DAILY_LIMIT, value).apply()

    var sendDelaySeconds: Int
        get() = prefs.getInt(KEY_SEND_DELAY, 2)
        set(value) = prefs.edit().putInt(KEY_SEND_DELAY, value).apply()

    var startHour: Int
        get() = prefs.getInt(KEY_START_HOUR, 8)
        set(value) = prefs.edit().putInt(KEY_START_HOUR, value).apply()

    var endHour: Int
        get() = prefs.getInt(KEY_END_HOUR, 22)
        set(value) = prefs.edit().putInt(KEY_END_HOUR, value).apply()

    var smsMessage: String
        get() = prefs.getString(KEY_SMS_MESSAGE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SMS_MESSAGE, value).apply()

    var sim1SubscriptionId: Int
        get() = prefs.getInt(KEY_SIM1_SUB_ID, -1)
        set(value) = prefs.edit().putInt(KEY_SIM1_SUB_ID, value).apply()

    var sim2SubscriptionId: Int
        get() = prefs.getInt(KEY_SIM2_SUB_ID, -1)
        set(value) = prefs.edit().putInt(KEY_SIM2_SUB_ID, value).apply()

    var sim1Name: String
        get() = prefs.getString(KEY_SIM1_NAME, "سیم 1") ?: "سیم 1"
        set(value) = prefs.edit().putString(KEY_SIM1_NAME, value).apply()

    var sim2Name: String
        get() = prefs.getString(KEY_SIM2_NAME, "سیم 2") ?: "سیم 2"
        set(value) = prefs.edit().putString(KEY_SIM2_NAME, value).apply()

    var isSending: Boolean
        get() = prefs.getBoolean(KEY_IS_SENDING, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_SENDING, value).apply()

    var dailySentToday: Int
        get() = prefs.getInt(KEY_DAILY_SENT_TODAY, 0)
        set(value) = prefs.edit().putInt(KEY_DAILY_SENT_TODAY, value).apply()

    var dailyResetDate: String
        get() = prefs.getString(KEY_DAILY_RESET_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DAILY_RESET_DATE, value).apply()
}
