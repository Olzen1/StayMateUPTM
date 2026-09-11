package com.staymate.uptm

import com.staymate.uptm.data.AppDatabase
import com.staymate.uptm.data.User
import com.staymate.uptm.data.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: User) = userDao.insert(user)

    suspend fun getUserById(uid: String): User? = userDao.getUserById(uid)

    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)

    suspend fun updateUserProfile(
        uid: String,
        name: String,
        course: String,
        semester: String,
        profileImage: String?
    ) = userDao.updateUser(uid, name, course, semester, profileImage)

    suspend fun deleteUser(uid: String) = userDao.deleteUser(uid)

    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
}