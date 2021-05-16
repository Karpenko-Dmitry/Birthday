package ru.mephi.birthday.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

@Entity()
@TypeConverters(UserStateConverter::class,UUIDConverter::class)
 data class Person (
        @PrimaryKey()
        val uuid: UUID,
        var nickName : String,
        var day : Byte,
        var month : Byte,
        var year : Short?,
        var state : UserState,
        var uri : String?
        )  {

        constructor(nickName: String, day : Byte,month : Byte, year : Short?,state: UserState,uri : String?) :
                this( UUID.randomUUID(),nickName, day,month, year, state, uri)
}


