package ru.mephi.birthday.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.mephi.birthday.context.MyApplication

class NotificationWorker(context : Context, params : WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val repository = (applicationContext as MyApplication).repository
        val list = repository.getListBirthdayToday()
        Log.i("Notification1", "doWork")
        return Result.success()
    }

}