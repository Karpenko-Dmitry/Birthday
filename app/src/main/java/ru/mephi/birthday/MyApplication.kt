package ru.mephi.birthday

import android.app.Application
import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import ru.mephi.birthday.database.AppDatabase

class MyApplication : Application() {

    val appScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getAppDatabase(this,appScope)}
    val repository by lazy {Repository(database.personDao())}

}