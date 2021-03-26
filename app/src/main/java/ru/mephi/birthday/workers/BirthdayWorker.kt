package ru.mephi.birthday.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import ru.mephi.birthday.context.MyApplication
import java.util.*

class BirthdayWorker (val context : Context, params : WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        Log.i("BirthdayNotification","BirthdayWorker")
        val pref = context.getSharedPreferences("NotificationManager",Context.MODE_PRIVATE)
        pref.edit().putString("BirtdayWorker",Date(System.currentTimeMillis()).toString()).commit()
        val date = pref.getLong( MyApplication.prefNotification,0)
        if (wasNotNotificationToday(date)) {
            val data = Data.Builder().putBoolean(NotificationWorker.DATA_KEY,true).build()
            val uploadWorkRequest: OneTimeWorkRequest =
                    OneTimeWorkRequestBuilder<NotificationWorker>().setInputData(data).build()
            WorkManager.getInstance(applicationContext).beginUniqueWork("birthday_search",
                    ExistingWorkPolicy.REPLACE,uploadWorkRequest).enqueue()
        }
        return Result.success()
    }
}

fun wasNotNotificationToday(date : Long) : Boolean {
    val nowday = GregorianCalendar()
    val day = GregorianCalendar()
    day.time = Date(date)
    return (nowday.get(Calendar.DAY_OF_YEAR) > day.get(Calendar.DAY_OF_YEAR)) ||
            (nowday.get(Calendar.YEAR) > day.get(Calendar.YEAR))
}