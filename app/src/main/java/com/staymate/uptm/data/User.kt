package com.staymate.uptm.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val uid: String,
    val email: String,
    val name: String,
    val course: String,
    val semester: String,
    val profileImage: String?,
    val createdAt: Long = System.currentTimeMillis()
)