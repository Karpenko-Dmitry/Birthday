package ru.mephi.birthday.context

import android.app.Application
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import ru.mephi.birthday.repository.Repository
import ru.mephi.birthday.database.AppDatabase
import ru.mephi.birthday.workers.SynchronizationWorker

class MyApplication : Application() {

    val appScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getAppDatabase(this)}
    val repository by lazy { Repository(database.personDao(),database.deletePersonDao()) }

    companion object {
        val prefNotification = "notification"
    }

    override fun onCreate() {
        super.onCreate()
        val db = Firebase.firestore
        db.firestoreSettings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
        val synchronizationWorkRequest : OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<SynchronizationWorker>().build()
        WorkManager.getInstance(this).beginUniqueWork("synchronization",
            ExistingWorkPolicy.REPLACE,synchronizationWorkRequest).enqueue()
    }


}