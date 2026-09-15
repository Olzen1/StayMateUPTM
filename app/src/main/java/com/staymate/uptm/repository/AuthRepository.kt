package com.staymate.uptm.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.staymate.uptm.utils.UptmConstants
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import com.staymate.uptm.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.collections.remove

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    /**
     * Validates if the email belongs to a UPTM student.
     * @return true if valid, false otherwise.
     */
    fun isUptmEmail(email: String): Boolean {
        return email.trim().endsWith(UptmConstants.EMAIL_DOMAIN, ignoreCase = true)
    }

    /**
     * Signs in with Email and Password.
     * If the user does not exist, it automatically creates the account.
     */
    suspend fun signInOrCreateWithEmail(email: String, password: String): Result<String> {
        return try {
            // 1. Try to sign in first
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(auth.currentUser?.uid ?: "")
        } catch (e: Exception) {
            // 2. If the error is "user-not-found", silently create the account
            if (e.message?.contains("user-not-found", ignoreCase = true) == true) {
                try {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    Result.success(auth.currentUser?.uid ?: "")
                } catch (createException: Exception) {
                    Result.failure(createException)
                }
            } else {
                // 3. Any other error (wrong password, network issue) is passed up
                Result.failure(e)
            }
        }
    }
    suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            // 1. Create a Firebase credential using the Google ID Token
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            // 2. Sign in to Firebase with this credential
            auth.signInWithCredential(credential).await()

            val user = auth.currentUser

            // 3. THE UPTM CHECK: Verify the email belongs to UPTM
            if (user != null && isUptmEmail(user.email ?: "")) {
                Result.success(user.uid)
            } else {
                // 4. REJECTION: If it's a personal Gmail or wrong domain, sign them out immediately!
                auth.signOut()
                Result.failure(Exception("Please use a valid UPTM student email (@student.uptm.edu.my)."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun doesUserProfileExist(uid: String): Boolean {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createUserProfile(profile: UserProfile): Result<Boolean> {
        return try {
            firestore.collection("users").document(profile.uid).set(profile).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun currentUid(): String? = auth.currentUser?.uid
    fun currentEmail(): String? = auth.currentUser?.email
    fun observeAuthUid(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener) // fires immediately with current user
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun signOut() {
        auth.signOut()
    }
    fun observeUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(UserProfile::class.java))
            }
        awaitClose { listener.remove() }
    }
}
