package ru.mephi.birthday.database

import androidx.room.*
import com.google.android.material.circularreveal.CircularRevealHelper
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM person WHERE personId = :id")
    suspend fun getById(id : Long) : Person

    @Query("SELECT * FROM person ORDER BY lastName")
    fun getPersonByAlphabetOrder(): Flow<List<Person>>

    @Query("SELECT * FROM person ORDER BY lastName")
    fun getPersonByAlphabetOrderList(): List<Person>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(person: Person) : Long

    @Update
    suspend fun update(person: Person)

    @Delete
    suspend fun delete(person: Person)
}