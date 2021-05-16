package ru.mephi.birthday.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

@Entity(tableName = "DeletePerson")
@TypeConverters(UUIDConverter::class)
data class DeletePerson(@PrimaryKey val uuid: UUID)