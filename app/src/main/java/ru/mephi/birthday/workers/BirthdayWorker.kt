package ru.mephi.birthday.workers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import androidx.work.impl.model.Preference
import ru.mephi.birthday.context.MyApplication
import java.util.*

class BirthdayWorker (val context : Context, params : WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val pref = context.getSharedPreferences("NotificationManager",Context.MODE_PRIVATE)
        pref.edit().putString("BirtdayWorker",Date(System.currentTimeMillis()).toString())
        val date = pref.getLong( MyApplication.prefNotification,0)
        if (wasNotNotificationToday(date)) {
            val uploadWorkRequest: OneTimeWorkRequest =
                    OneTimeWorkRequestBuilder<NotificationWorker>().build()
            WorkManager.getInstance(applicationContext).beginUniqueWork("birthday_search",
                    ExistingWorkPolicy.REPLACE,uploadWorkRequest).enqueue()
        }
        return Result.success()
    }

    private fun wasNotNotificationToday(date : Long) : Boolean {
        val nowday = GregorianCalendar()
        val day = GregorianCalendar()
        day.time = Date(date)
        return (nowday.get(Calendar.DAY_OF_YEAR) > day.get(Calendar.DAY_OF_YEAR)) ||
                (nowday.get(Calendar.YEAR) > day.get(Calendar.YEAR))
    }

}