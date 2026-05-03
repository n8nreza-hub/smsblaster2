package com.smsblaster.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.smsblaster.R
import com.smsblaster.databinding.ActivityMainBinding
import com.smsblaster.service.SmsService
import com.smsblaster.ui.failed.FailedActivity
import com.smsblaster.ui.report.ReportActivity
import com.smsblaster.util.AppPrefs
import com.smsblaster.util.SimManager
import com.smsblaster.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var prefs: AppPrefs

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importExcel(it) }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "همه مجوزها تایید شدند ✅", Toast.LENGTH_SHORT).show()
            loadSimInfo()
        } else {
            Toast.makeText(this, "برخی مجوزها رد شدند!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPrefs(this)

        requestPermissions()
        setupUI()
        observeViewModel()
        loadSimInfo()
        loadSettings()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun setupUI() {
        // دکمه انتخاب فایل
        binding.btnSelectFile.setOnClickListener {
            pickFileLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        }

        // دکمه شروع
        binding.btnStart.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isEmpty()) {
                Toast.makeText(this, "لطفاً متن پیامک را وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveSettings()
            prefs.smsMessage = message
            startSmsService()
        }

        // دکمه توقف
        binding.btnStop.setOnClickListener {
            stopSmsService()
        }

        // دکمه گزارش
        binding.btnReport.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        // دکمه ارسال نشده‌ها
        binding.btnFailed.setOnClickListener {
            startActivity(Intent(this, FailedActivity::class.java))
        }

        // ذخیره تنظیمات با تغییر
        binding.btnSaveSettings.setOnClickListener {
            saveSettings()
            Toast.makeText(this, "تنظیمات ذخیره شد ✅", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.totalContacts.observe(this) { total ->
            binding.tvTotalContacts.text = "تعداد کل شماره‌ها: $total"
        }

        viewModel.sentCount.observe(this) { sent ->
            val total = viewModel.totalContacts.value ?: 0
            binding.tvProgress.text = "ارسال شده: $sent از $total"
            if (total > 0) {
                binding.progressBar.progress = (sent * 100 / total)
            }
        }

        viewModel.importStatus.observe(this) { status ->
            binding.tvImportStatus.text = status
        }

        viewModel.isImporting.observe(this) { importing ->
            binding.btnSelectFile.isEnabled = !importing
            if (importing) {
                binding.tvImportStatus.text = "در حال وارد کردن..."
            }
        }

        viewModel.progress.observe(this) { progress ->
            progress?.let {
                val statusText = if (it.isRunning) "در حال ارسال..." else "متوقف"
                binding.tvSendingStatus.text = "وضعیت: $statusText"
                binding.btnStart.isEnabled = !it.isRunning
                binding.btnStop.isEnabled = it.isRunning
            }
        }
    }

    private fun loadSimInfo() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val sims = SimManager.getAvailableSims(this)

            if (sims.isNotEmpty()) {
                val sim1 = sims.getOrNull(0)
                val sim2 = sims.getOrNull(1)

                sim1?.let {
                    prefs.sim1SubscriptionId = it.subscriptionId
                    prefs.sim1Name = it.displayName
                    binding.tvSim1Info.text = "سیم ۱: ${it.displayName} - ${it.operatorName}"
                }

                sim2?.let {
                    prefs.sim2SubscriptionId = it.subscriptionId
                    prefs.sim2Name = it.displayName
                    binding.tvSim2Info.text = "سیم ۲: ${it.displayName} - ${it.operatorName}"
                } ?: run {
                    binding.tvSim2Info.text = "سیم ۲: موجود نیست"
                }
            } else {
                binding.tvSim1Info.text = "سیم‌کارتی پیدا نشد"
            }
        }
    }

    private fun loadSettings() {
        binding.etBlockSize.setText(prefs.blockSize.toString())
        binding.etDailyLimit.setText(prefs.dailyLimit.toString())
        binding.etDelay.setText(prefs.sendDelaySeconds.toString())
        binding.etStartHour.setText(prefs.startHour.toString())
        binding.etEndHour.setText(prefs.endHour.toString())
        binding.etMessage.setText(prefs.smsMessage)
    }

    private fun saveSettings() {
        prefs.blockSize = binding.etBlockSize.text.toString().toIntOrNull() ?: 250
        prefs.dailyLimit = binding.etDailyLimit.text.toString().toIntOrNull() ?: 500
        prefs.sendDelaySeconds = binding.etDelay.text.toString().toIntOrNull() ?: 2
        prefs.startHour = binding.etStartHour.text.toString().toIntOrNull() ?: 8
        prefs.endHour = binding.etEndHour.text.toString().toIntOrNull() ?: 22
        prefs.smsMessage = binding.etMessage.text.toString()
    }

    private fun startSmsService() {
        val intent = Intent(this, SmsService::class.java).apply {
            action = SmsService.ACTION_START
        }
        startForegroundService(intent)
        binding.tvSendingStatus.text = "وضعیت: در حال ارسال..."
        binding.btnStart.isEnabled = false
        binding.btnStop.isEnabled = true

        // به‌روزرسانی UI از سرویس
        SmsService.onStatusUpdate = { status ->
            runOnUiThread {
                binding.tvSendingStatus.text = "وضعیت: $status"
            }
        }
    }

    private fun stopSmsService() {
        val intent = Intent(this, SmsService::class.java).apply {
            action = SmsService.ACTION_STOP
        }
        startService(intent)
        binding.tvSendingStatus.text = "وضعیت: متوقف شد"
        binding.btnStart.isEnabled = true
        binding.btnStop.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        loadSimInfo()
        SmsService.onStatusUpdate = { status ->
            runOnUiThread {
                binding.tvSendingStatus.text = "وضعیت: $status"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        SmsService.onStatusUpdate = null
    }
}
