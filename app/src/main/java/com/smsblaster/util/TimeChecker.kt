package com.smsblaster.util

import java.text.SimpleDateFormat
import java.util.*

object TimeChecker {

    /**
     * بررسی اینکه الان در بازه مجاز ارسال هستیم یا نه
     */
    fun isInAllowedTime(startHour: Int, endHour: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        return currentHour in startHour until endHour
    }

    /**
     * گرفتن تاریخ امروز به صورت رشته برای مقایسه
     */
    fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * تبدیل timestamp به رشته قابل نمایش فارسی
     */
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
