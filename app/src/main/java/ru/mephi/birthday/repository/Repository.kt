package ru.mephi.birthday.repository

import android.util.Log
import android.widget.Toast
import androidx.annotation.WorkerThread
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.joda.time.Days
import org.joda.time.Instant
import ru.mephi.birthday.context.AddPersonFragment.*
import ru.mephi.birthday.database.*
import java.util.*
import kotlin.collections.ArrayList

class Repository(private val personDao: PersonDao,private val deletePersonDao: DeletePersonDao) {

    val allPerson: Flow<List<Person>> = personDao.getPersonByAlphabetOrder()
    val db = Firebase.firestore

    fun insert(person: Person)  {
        GlobalScope.async(Dispatchers.IO) {
            personDao.insert(person)
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && person.state != UserState.INVALID) {
                setPersonToFirebase(person, user)
            }
        }
    }

    fun insertToLocalDB(person: Person) {
        GlobalScope.async(Dispatchers.IO) {
            personDao.insert(person)
        }
    }

    private fun setPersonToFirebase(person: Person, user: FirebaseUser) {
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
        if (person.facebookId != null) {
            prs.put("facebookId", person.facebookId)
        }
        val uuid = person.uuid
        db.collection("Users").document(user.uid).collection("birthdays")
            .document(uuid.toString()).set(prs, SetOptions.merge())
            .addOnCompleteListener { updateState(uuid, UserState.SYNCHRONIZED) }
            //.addOnFailureListener { updateState(uuid,UserState.NOT_SYNCHRONIZED ) }
    }

    private fun deletePersonFromFirebase(person: Person, user: FirebaseUser) {
        val uuid = person.uuid
        Log.d("FirebaseDelete","Del")
        db.collection("Users")
            .document(user.uid)
            .collection("birthdays")
            .document(uuid.toString())
            .delete().addOnCompleteListener {
                deleteFromDeletePerson(uuid)
            }
    }

    fun updateState(uuid: UUID, state: UserState) {
        GlobalScope.async(Dispatchers.IO) {
            personDao.updateState(uuid, state)
        }
    }

    fun update(person: Person) {
        GlobalScope.async(Dispatchers.IO) {
            personDao.update(person)
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && person.state != UserState.INVALID) {
                setPersonToFirebase(person, user)
            }
        }
    }

    fun delete(person: Person) {
        GlobalScope.async(Dispatchers.IO) {
            personDao.delete(person)
            insertToDeletePerson(person)
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                deletePersonFromFirebase(person, user)
            }
        }
    }

    private fun insertToDeletePerson(person: Person) {
        if (person.state == UserState.SYNCHRONIZED) {
            GlobalScope.async(Dispatchers.IO) {
                deletePersonDao.insert(DeletePerson(person.uuid))
            }
        }
    }

    fun deleteFromDeletePerson(uuid: UUID) {
        GlobalScope.async(Dispatchers.IO) {
            deletePersonDao.delete(DeletePerson(uuid))
        }
    }

    suspend fun getDeletePerson() = deletePersonDao.getDeletePerson()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete() = personDao.delete()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getPerson(uuid: UUID) = personDao.getById(uuid)

    suspend fun  getPersonWithoutSynchronization() = personDao.getPersonWithoutSynchronization()


    fun getListBirthdayToday(): List<Person> {
        val person = personDao.getPersonByAlphabetOrderList()
        val list = ArrayList<Person>()
        for (p in person) {
            if (getTimeForBirthday(DateWithNullYear(p.day, p.month, p.year)) == 0) {
                list.add(p)
            }
        }
        return list
    }

    companion object {

        fun getTimeForBirthday(birthday: DateWithNullYear): Int {
            val curMilliSec = System.currentTimeMillis()
            val birthCalendar = GregorianCalendar()
            birthCalendar.set(Calendar.DAY_OF_MONTH, birthday.day.toInt())
            birthCalendar.set(Calendar.MONTH, birthday.month.toInt())
            birthCalendar.set(
                Calendar.YEAR,
                if (birthday.year != null) birthday.year.toInt() else 1970
            )
            val nowdayCalendar = GregorianCalendar()
            nowdayCalendar.time = Date(curMilliSec)
            birthCalendar.add(
                Calendar.YEAR,
                nowdayCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
            )
            if (birthCalendar.before(nowdayCalendar)) {
                birthCalendar.add(Calendar.YEAR, 1)
            }
            val daysBetween = Days.daysBetween(
                Instant(nowdayCalendar.time),
                Instant(birthCalendar.time)
            ).days
            val isLeap = nowdayCalendar.isLeapYear(nowdayCalendar.get(Calendar.YEAR))
            if (isLeap) {
                return (daysBetween + 1) % 366
            } else {
                return (daysBetween + 1) % 365
            }
        }

        fun getTimeForBirthdayString(birthday: DateWithNullYear): String {
            return getTimeForBirthday(birthday).toString() + " days"
        }
    }
}

