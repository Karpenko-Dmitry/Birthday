package ru.mephi.birthday.database

import androidx.room.*
import com.google.android.material.circularreveal.CircularRevealHelper
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface PersonDao {

    @TypeConverters(UUIDConverter::class)
    @Query("SELECT * FROM person WHERE uuid = :uuid")
    suspend fun getById(uuid: UUID) : Person

    @Query("SELECT * FROM person ORDER BY nickName")
    fun getPersonByAlphabetOrder(): Flow<List<Person>>

    @Query("SELECT * FROM person ORDER BY nickName")
    fun getPersonByAlphabetOrderList(): List<Person>

    @TypeConverters(UserStateConverter::class)
    @Query("SELECT * FROM person WHERE state = :state")
    suspend fun getPersonWithoutSynchronization(state: UserState = UserState.NOT_SYNCHRONIZED): List<Person>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(person: Person)

    @Update
    suspend fun update(person: Person)

    @TypeConverters(UserStateConverter::class,UUIDConverter::class)
    @Query("UPDATE person SET state = :newState WHERE uuid = :uuid")
    suspend fun updateState(uuid: UUID,newState: UserState)

    @Delete
    suspend fun delete(person: Person)

    @Query("DELETE FROM person")
    suspend fun delete()
}