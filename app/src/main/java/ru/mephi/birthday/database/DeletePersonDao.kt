package ru.mephi.birthday.database

import androidx.room.*
import java.util.*

@Dao
@TypeConverters(UUIDConverter::class)
interface DeletePersonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deletePerson: DeletePerson)

    @Delete
    suspend fun delete(deletePerson: DeletePerson)

    @Query("SELECT * FROM DeletePerson")
    suspend fun getDeletePerson(): List<DeletePerson>
}