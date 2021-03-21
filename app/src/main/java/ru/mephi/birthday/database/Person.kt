package ru.mephi.birthday.database

import android.icu.util.Calendar
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity()
 data class Person (
        @PrimaryKey(autoGenerate = true)
        val personId : Long,
        var firstName : String,
        var lastName : String,
        var birthday : Long
         )  {
        constructor(firstName: String, lastName: String, birthday : Long) : this(0,firstName, lastName, birthday)
}


