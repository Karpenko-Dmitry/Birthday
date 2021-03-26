package ru.mephi.birthday.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.mephi.birthday.R
import ru.mephi.birthday.context.MyApplication
import ru.mephi.birthday.database.Person
import java.util.*

class NotificationWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {

    val notManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        val DATA_KEY  = "key1"
        val CHANNEL_ID = "ru.mephi.birthday.notification"
        val SUMMARY_ID = 0
        val GROUP_BIRTHDAY = "ru.mephi.birthday.notification_group"
    }

    override fun doWork(): Result {
        Log.i("BirthdayNotification", "NotificationWorker")
        val pref = context.getSharedPreferences("NotificationManager", Context.MODE_PRIVATE)
        pref.edit().putString("NotificationWorker", Date(System.currentTimeMillis()).toString()).commit()
        createNotificationChannel()
        if (inputData.getBoolean(DATA_KEY, false)) {
            notifyBirthdayWithLog(pref)
        } else {
            val date = pref.getLong(MyApplication.prefNotification, 0)
            if (wasNotNotificationToday(date)) {
                notifyBirthdayWithLog(pref)
            }
        }
        return Result.success()
    }

    private fun notifyBirthdayWithLog(pref: SharedPreferences) {
        notifyBirthday()
        pref.edit().putLong(MyApplication.prefNotification, System.currentTimeMillis()).commit()
    }

    private fun notifyBirthday() {
        val list = getListBirthday()
        for (person in list) {
            showNotification(person)
        }
        if (list.size > 1) {
            showNotificationGroup(list.size)
        }
    }

    private fun getListBirthday() = (applicationContext as MyApplication).repository.getListBirthdayToday()

    private fun showNotification(person: Person) {
        val notBuilder = NotificationCompat.Builder(context, CHANNEL_ID).
            setContentTitle("Birthday:").
            setContentText("${person.nickName}  has Birthday today").
            setSmallIcon(R.drawable.ic_user).
            setGroup(GROUP_BIRTHDAY).
            setPriority(2).setColorized(true).
            setColor(Color.YELLOW).
            setLights(Color.argb(255, 0, 0, 255), 1000, 1000).
            setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
        notManager.notify(person.personId, notBuilder.build())
    }

    private fun showNotificationGroup(n: Int) {
        val groupNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Birthdays")
                .setContentText("${n} new notifications")
                .setSmallIcon(R.drawable.ic_ballon)
                .setGroup(GROUP_BIRTHDAY)
                .setGroupSummary(true)
                .build()
        notManager.notify(SUMMARY_ID, groupNotification)
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notChannel = NotificationChannel(CHANNEL_ID, "Birthday notification", NotificationManager.IMPORTANCE_HIGH)
            notChannel.enableLights(true)
            notChannel.setLightColor(Color.RED)
            notChannel.enableVibration(true)
            notChannel.setDescription("Birthday notification")
            notManager.createNotificationChannel(notChannel)
        }
    }



}