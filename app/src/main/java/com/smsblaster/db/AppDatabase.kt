package com.smsblaster.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smsblaster.db.dao.ContactDao
import com.smsblaster.db.dao.FailedSmsDao
import com.smsblaster.db.dao.ProgressDao
import com.smsblaster.db.entity.Contact
import com.smsblaster.db.entity.FailedSms
import com.smsblaster.db.entity.Progress

@Database(
    entities = [Contact::class, Progress::class, FailedSms::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun progressDao(): ProgressDao
    abstract fun failedSmsDao(): FailedSmsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sms_blaster_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
