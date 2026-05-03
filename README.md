# 📱 پیامک انبوه دو سیم‌کارته — SMSBlaster

پروژه Android Studio کامل برای ارسال پیامک انبوه با دو سیم‌کارت

---

## 📁 ساختار پروژه

```
SMSBlaster/
├── build.gradle                          ← Gradle سطح پروژه
├── settings.gradle
├── gradle.properties
└── app/
    ├── build.gradle                      ← Dependencies اصلی
    └── src/main/
        ├── AndroidManifest.xml           ← تمام Permissions
        ├── java/com/smsblaster/
        │   ├── db/
        │   │   ├── AppDatabase.kt        ← Room Database
        │   │   ├── entity/
        │   │   │   ├── Contact.kt        ← جدول شماره‌ها
        │   │   │   ├── Progress.kt       ← جدول پیشرفت
        │   │   │   └── FailedSms.kt      ← جدول ناموفق‌ها
        │   │   └── dao/
        │   │       ├── ContactDao.kt
        │   │       ├── ProgressDao.kt
        │   │       └── FailedSmsDao.kt
        │   ├── repository/
        │   │   └── SmsRepository.kt      ← لایه داده
        │   ├── service/
        │   │   ├── SmsService.kt         ← Foreground Service اصلی
        │   │   └── BootReceiver.kt       ← ادامه بعد از ریبوت
        │   ├── ui/
        │   │   ├── main/MainActivity.kt  ← صفحه اصلی
        │   │   ├── report/ReportActivity.kt ← گزارش
        │   │   └── failed/
        │   │       ├── FailedActivity.kt
        │   │       └── FailedAdapter.kt
        │   ├── viewmodel/
        │   │   ├── MainViewModel.kt
        │   │   ├── ReportViewModel.kt
        │   │   └── FailedViewModel.kt
        │   └── util/
        │       ├── AppPrefs.kt           ← ذخیره تنظیمات
        │       ├── ExcelReader.kt        ← خواندن اکسل با Apache POI
        │       ├── SimManager.kt         ← مدیریت سیم‌کارت‌ها
        │       └── TimeChecker.kt        ← بازه زمانی
        └── res/
            ├── layout/
            │   ├── activity_main.xml
            │   ├── activity_report.xml
            │   ├── activity_failed.xml
            │   └── item_failed.xml
            └── values/
                ├── strings.xml
                ├── colors.xml
                └── themes.xml
```

---

## ⚙️ Dependencies مهم

```groovy
// Room Database
implementation 'androidx.room:room-runtime:2.6.1'
kapt 'androidx.room:room-compiler:2.6.1'

// Apache POI (خواندن اکسل)
implementation 'org.apache.poi:poi:5.2.3'
implementation 'org.apache.poi:poi-ooxml:5.2.3'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
```

---

## 🔧 مراحل راه‌اندازی

### 1. باز کردن پروژه
- Android Studio را باز کنید
- روی "Open" کلیک کنید
- پوشه SMSBlaster را انتخاب کنید

### 2. Sync کردن Gradle
- منتظر بمانید تا Gradle sync شود
- اگر خطا دیدید → File → Invalidate Caches → Restart

### 3. اجرا روی Redmi 8
- USB Debugging را فعال کنید
- گوشی را وصل کنید
- Run کنید ▶

---

## 📋 نکات مهم

### درباره سیم‌کارت
- برنامه به‌صورت خودکار SubscriptionId هر سیم را می‌خواند
- از `SmsManager.getSmsManagerForSubscriptionId()` استفاده می‌شود
- در Redmi 8 با Dual SIM کاملاً کار می‌کند

### درباره فایل اکسل
- فرمت: `.xlsx`
- شماره‌ها در ستون B (ستون دوم)
- ردیف اول می‌تواند هدر باشد (به‌صورت خودکار تشخیص داده می‌شود)

### درباره منطق بلوک‌بندی
```
بلوک = 250

بلوک اول:
  سیم 1 → شماره 1 تا 250
  سیم 2 → شماره 251 تا 500

بلوک دوم:
  سیم 1 → شماره 501 تا 750
  سیم 2 → شماره 751 تا 1000
```

### درباره ذخیره پیشرفت
- هر پیامک ارسال شده بلافاصله در دیتابیس ذخیره می‌شود
- اگر گوشی خاموش شود، بعد از روشن شدن از همانجا ادامه می‌دهد
- شمارنده تا پایان کل فایل ریست نمی‌شود

---

## 🚀 ویژگی‌ها

- ✅ ارسال بلوکی با دو سیم‌کارت
- ✅ ذخیره دائمی پیشرفت در SQLite/Room
- ✅ ادامه بعد از خاموش شدن گوشی
- ✅ Foreground Service (سرویس پس‌زمینه)
- ✅ بدون ارسال تکراری
- ✅ فاصله قابل تنظیم بین ارسال‌ها
- ✅ محدودیت روزانه
- ✅ بازه زمانی مجاز
- ✅ گزارش کامل
- ✅ مدیریت و ارسال مجدد ارسال‌های ناموفق
- ✅ رابط فارسی RTL
