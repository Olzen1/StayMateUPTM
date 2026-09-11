package com.staymate.uptm.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.staymate.uptm.data.User
import com.staymate.uptm.data.UserDao

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}