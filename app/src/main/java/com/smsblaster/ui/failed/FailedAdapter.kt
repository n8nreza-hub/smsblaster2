package com.smsblaster.ui.failed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smsblaster.databinding.ItemFailedBinding
import com.smsblaster.db.entity.FailedSms
import com.smsblaster.util.TimeChecker

class FailedAdapter(
    private val onRetry: (FailedSms) -> Unit
) : ListAdapter<FailedSms, FailedAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemFailedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FailedSms) {
            binding.tvPhone.text = item.phoneNumber
            binding.tvReason.text = "دلیل: ${item.reason.ifEmpty { "نامشخص" }}"
            binding.tvDate.text = "تاریخ: ${TimeChecker.formatTimestamp(item.date)}"
            binding.tvRetryCount.text = "تعداد تلاش: ${item.retryCount}"
            binding.btnRetry.setOnClickListener { onRetry(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFailedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<FailedSms>() {
        override fun areItemsTheSame(oldItem: FailedSms, newItem: FailedSms) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FailedSms, newItem: FailedSms) =
            oldItem == newItem
    }
}
