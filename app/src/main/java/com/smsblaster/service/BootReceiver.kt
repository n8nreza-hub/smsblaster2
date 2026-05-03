package com.smsblaster.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smsblaster.util.AppPrefs

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = AppPrefs(context)

            // اگر قبل از خاموش شدن در حال ارسال بودیم، دوباره شروع کن
            if (prefs.isSending) {
                val serviceIntent = Intent(context, SmsService::class.java).apply {
                    action = SmsService.ACTION_START
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
