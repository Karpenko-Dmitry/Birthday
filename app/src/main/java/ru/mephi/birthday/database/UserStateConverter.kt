package ru.mephi.birthday.database

import androidx.room.TypeConverter

class UserStateConverter {

    @TypeConverter
    fun fromState(state: UserState) : Int {
        return state.ordinal
    }

    @TypeConverter
    fun toState(ordinal : Int) : UserState {
        return UserState.values()[ordinal]
    }
}