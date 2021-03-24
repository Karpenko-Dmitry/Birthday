package ru.mephi.birthday.context

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import ru.mephi.birthday.repository.Repository
import ru.mephi.birthday.database.AppDatabase

class MyApplication : Application() {

    val appScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getAppDatabase(this,appScope)}
    val repository by lazy { Repository(database.personDao()) }

    companion object {
        val prefNotification = "notification"
    }

}