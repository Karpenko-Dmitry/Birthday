package ru.mephi.birthday.repository

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.WorkerThread
import androidx.navigation.findNavController
import kotlinx.coroutines.flow.Flow
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.Period
import org.joda.time.PeriodType
import ru.mephi.birthday.R
import ru.mephi.birthday.adapters.PersonListAdapter
import ru.mephi.birthday.context.MainFragmentDirections
import ru.mephi.birthday.database.Person
import ru.mephi.birthday.database.PersonDao
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList

class Repository(private val personDao: PersonDao) {

    val allPerson : Flow<List<Person>> = personDao.getPersonByAlphabetOrder()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(person : Person) = personDao.insert(person)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(person : Person) = personDao.update(person)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(person : Person) = personDao.delete(person)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getPerson(id : Int) = personDao.getById(id)

    fun getListBirthdayToday() : List<Person> {
        val person = personDao.getPersonByAlphabetOrderList()
        val list = ArrayList<Person>()
        for (p in person) {
            if (getTimeForBirthday(p.birthday) == 0) {
                list.add(p)
            }
        }
        return list
    }

    companion object {

       fun getTimeForBirthday(birthday : Long) : Int {
            val curMilliSec= System.currentTimeMillis()
            val birthCalendar = GregorianCalendar()
            birthCalendar.time = Date(birthday)
            val nowdayCalendar = GregorianCalendar()
            nowdayCalendar.time = Date(curMilliSec)
            birthCalendar.add(Calendar.YEAR,nowdayCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR))
            if (birthCalendar.before(nowdayCalendar)) {
                birthCalendar.add(Calendar.YEAR,1)
            }
            val daysBetween = Days.daysBetween(Instant(nowdayCalendar.time),
                   Instant(birthCalendar.time)).days
            val isLeap = nowdayCalendar.isLeapYear(nowdayCalendar.get(Calendar.YEAR))
            if (isLeap) {
                return (daysBetween + 1) % 366
            } else {
                return (daysBetween + 1) % 365
            }
        }

        fun getTimeForBirthdayString(birthday : Long) : String {
            return getTimeForBirthday(birthday).toString() + " days"
        }
    }

}