package ru.mephi.birthday.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Person::class,DeletePerson::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun personDao() : PersonDao
    abstract fun deletePersonDao() : DeletePersonDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getAppDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "person_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}