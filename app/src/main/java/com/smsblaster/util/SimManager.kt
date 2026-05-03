package com.smsblaster.util

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager

data class SimInfo(
    val subscriptionId: Int,
    val displayName: String,
    val slotIndex: Int,
    val operatorName: String
)

object SimManager {

    @SuppressLint("MissingPermission")
    fun getAvailableSims(context: Context): List<SimInfo> {
        val sims = mutableListOf<SimInfo>()

        try {
            val subscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

            val activeSubscriptions = subscriptionManager.activeSubscriptionInfoList
                ?: return emptyList()

            for (sub in activeSubscriptions) {
                sims.add(
                    SimInfo(
                        subscriptionId = sub.subscriptionId,
                        displayName = sub.displayName?.toString() ?: "سیم ${sub.simSlotIndex + 1}",
                        slotIndex = sub.simSlotIndex,
                        operatorName = sub.carrierName?.toString() ?: "نامشخص"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return sims.sortedBy { it.slotIndex }
    }

    @SuppressLint("MissingPermission")
    fun getSimCount(context: Context): Int {
        return try {
            val subscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            subscriptionManager.activeSubscriptionInfoList?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
