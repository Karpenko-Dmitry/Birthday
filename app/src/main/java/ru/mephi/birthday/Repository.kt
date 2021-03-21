package ru.mephi.birthday

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import ru.mephi.birthday.database.Person
import ru.mephi.birthday.database.PersonDao

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
    suspend fun getPerson(id : Long) = personDao.getById(id)


}