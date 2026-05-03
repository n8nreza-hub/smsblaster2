package com.smsblaster.ui.report

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.smsblaster.databinding.ActivityReportBinding
import com.smsblaster.util.AppPrefs
import com.smsblaster.viewmodel.ReportViewModel

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private val viewModel: ReportViewModel by viewModels()
    private lateinit var prefs: AppPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPrefs(this)
        supportActionBar?.title = "گزارش ارسال"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        observeViewModel()
        binding.btnRefresh.setOnClickListener { viewModel.refresh() }
    }

    private fun observeViewModel() {
        viewModel.totalCount.observe(this) { total ->
            binding.tvTotal.text = "تعداد کل: $total"
        }

        viewModel.sim1Count.observe(this) { count ->
            binding.tvSim1Sent.text = "ارسال شده با سیم ۱: $count"
        }

        viewModel.sim2Count.observe(this) { count ->
            binding.tvSim2Sent.text = "ارسال شده با سیم ۲: $count"
        }

        viewModel.failedCount.observe(this) { count ->
            binding.tvFailed.text = "ارسال ناموفق: $count"
        }

        viewModel.progress.observe(this) { progress ->
            progress?.let {
                binding.tvLastIndex.text = "آخرین ایندکس: ${it.lastSentIndex}"
                binding.tvCurrentBlock.text = "بلوک فعلی: ${it.currentBlock}"
                binding.tvTotalSent.text = "کل ارسال شده: ${it.totalSent}"
                val statusStr = when {
                    it.fileCompleted -> "تمام شد ✅"
                    it.isRunning -> "در حال ارسال..."
                    else -> "متوقف"
                }
                binding.tvStatus.text = "وضعیت: $statusStr"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
