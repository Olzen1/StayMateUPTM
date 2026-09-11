package com.staymate.uptm.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.staymate.uptm.data.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserById(uid: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("UPDATE users SET name = :name, course = :course, semester = :semester, profileImage = :profileImage WHERE uid = :uid")
    suspend fun updateUser(
        uid: String,
        name: String,
        course: String,
        semester: String,
        profileImage: String?
    )

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteUser(uid: String)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
}