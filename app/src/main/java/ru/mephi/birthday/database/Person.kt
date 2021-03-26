package ru.mephi.birthday.database

import android.icu.util.Calendar
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity()
 data class Person (
        @PrimaryKey(autoGenerate = true)
        val personId : Int,
        var nickName : String,
        var birthday : Long
         )  {
        constructor(nickName: String, birthday : Long) : this(0,nickName, birthday)
}


