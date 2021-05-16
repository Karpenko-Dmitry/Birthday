package ru.mephi.birthday.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import ru.mephi.birthday.context.MyApplication
import ru.mephi.birthday.database.DeletePerson
import ru.mephi.birthday.database.Person
import ru.mephi.birthday.database.UserState
import java.util.*
import kotlin.collections.HashMap

class SynchronizationWorker(val context: Context, params: WorkerParameters) :
    Worker(context, params) {

    private val rep = (context.applicationContext as MyApplication).repository
    private val user = FirebaseAuth.getInstance().currentUser
    private val db = Firebase.firestore

    override fun doWork(): Result {
        if (user != null) {
            deleteSynchronization()
            setPersonSynchronization()
            updateSynchronization()
        } else {
            Result.failure()
        }
        return Result.success()
    }

    private fun deleteSynchronization() {
        GlobalScope.async(Dispatchers.IO) {
            val list = rep.getDeletePerson()
            for (person in list) {
                db.collection("Users")
                    .document(user!!.uid)
                    .collection("birthdays")
                    .document(person.uuid.toString())
                    .delete()
                    .addOnCompleteListener { rep.deleteFromDeletePerson(person.uuid) }
            }
        }
    }

    private fun setPersonSynchronization() {
        db.collection("Users")
            .document(user!!.uid)
            .collection("birthdays")
            .get()
            .addOnSuccessListener { result ->
                GlobalScope.async(Dispatchers.IO) {
                    for (document in result) {
                        if (rep.getPerson(UUID.fromString(document.id)) == null) {
                            val name: String = document.getField("name")!!
                            val day: Int = document.getField("day")!!
                            val month: Int = document.getField("month")!!
                            val year: Int? = document.getField("year")
                            val uri: String? = document.getField("uri")
                            val person = Person(
                                name, day.toByte(), month.toByte(),
                                year?.toShort(), UserState.SYNCHRONIZED, uri
                            )
                            rep.insertToLocalDB(person)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                //Result.failure()
            }
    }

    private fun updateSynchronization() {
        GlobalScope.async(Dispatchers.IO) {
            val list = rep.getPersonWithoutSynchronization()
            for (person in list) {
                val uuid = person.uuid
                db.collection("Users")
                    .document(user.uid).collection("birthdays")
                    .document(uuid.toString()).set(getPersonHashMap(person), SetOptions.merge())
                    .addOnCompleteListener { rep.updateState(uuid, UserState.SYNCHRONIZED) }
            }
        }
    }

    private fun getPersonHashMap(person: Person) : HashMap<String,Any> {
        val prs = hashMapOf(
            "name" to person.nickName,
            "day" to person.day.toInt(),
            "month" to person.month.toInt()
        )
        if (person.year != null) {
            prs.put("year", person.year!!.toInt())
        }
        if (person.uri != null) {
            prs.put("uri", person.uri!!)
        }
        return prs
    }




}