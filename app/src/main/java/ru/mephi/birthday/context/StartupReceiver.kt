package ru.mephi.birthday.context

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import ru.mephi.birthday.workers.BirthdayWorker
import ru.mephi.birthday.workers.NotificationWorker
import java.util.concurrent.TimeUnit

class StartupReceiver() : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("BirthdayNotification", "onReceive")
        val notificationWorkRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<NotificationWorker>().build()
        val birthdaySearchWorkRequest : PeriodicWorkRequest = PeriodicWorkRequestBuilder<BirthdayWorker>(15,TimeUnit.MINUTES).build()
        WorkManager.getInstance(context!!).beginUniqueWork("notification",
                                ExistingWorkPolicy.REPLACE,notificationWorkRequest).enqueue()
        WorkManager.getInstance(context!!).enqueue(birthdaySearchWorkRequest)

    }
}