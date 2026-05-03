package com.smsblaster.ui.failed

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.smsblaster.databinding.ActivityFailedBinding
import com.smsblaster.viewmodel.FailedViewModel

class FailedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFailedBinding
    private val viewModel: FailedViewModel by viewModels()
    private lateinit var adapter: FailedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFailedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "ارسال نشده‌ها"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        observeViewModel()

        binding.btnRetryAll.setOnClickListener {
            viewModel.retryAll()
            Toast.makeText(this, "در حال ارسال مجدد...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = FailedAdapter { failedSms ->
            viewModel.retrySingle(failedSms)
        }
        binding.rvFailed.layoutManager = LinearLayoutManager(this)
        binding.rvFailed.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.failedList.observe(this) { list ->
            adapter.submitList(list)
            binding.tvFailedCount.text = "تعداد: ${list.size} مورد"
        }

        viewModel.retryStatus.observe(this) { status ->
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
