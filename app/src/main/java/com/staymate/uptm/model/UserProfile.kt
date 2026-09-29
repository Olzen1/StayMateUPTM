package com.staymate.uptm.model

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val course: String = "",

    val semester: String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)