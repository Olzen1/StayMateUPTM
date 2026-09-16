package com.staymate.uptm.repository

import android.content.Context

import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
import com.staymate.uptm.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.staymate.uptm.utils.UptmConstants
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import com.staymate.uptm.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.collections.remove
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
class AuthRepository {


    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun isUptmEmail(email: String): Boolean {
        return email.trim().endsWith(UptmConstants.EMAIL_DOMAIN, ignoreCase = true)
    }
    suspend fun signInWithEmail(email: String, password: String): Result<String> { // sign-in ONLY: no auto-create anymore, registration lives behind the Google door now
        return try {
            auth.signInWithEmailAndPassword(email, password).await() // try the existing account
            Result.success(auth.currentUser?.uid ?: "") // hand back the uid on success
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) { // stable error codes from the Firebase backend
                "ERROR_USER_NOT_FOUND", // protection OFF: email unknown
                "ERROR_INVALID_CREDENTIAL" -> // protection ON: unknown email OR wrong password share this code
                    Result.failure(Exception("No account found or wrong password. First time? Continue with Google.")) // friendly + privacy-safe: never reveals WHICH emails exist
                else -> Result.failure(e) // disabled account, network, anything else: pass up untouched
            }
        } catch (e: Exception) {
            Result.failure(e) // non-Firebase failures
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

    fun signOut(context: Context) { // signs out of Firebase AND clears Google's cached default account for this app
        auth.signOut() // Firebase side: observeAuthUid flips to null → router sends us to Login
        val gso = GoogleSignInOptions.Builder(DEFAULT_SIGN_IN) // same options shape as the login client
            .requestIdToken(context.getString(R.string.default_web_client_id)) // read the generated ID through the passed Context
            .requestEmail() // same email request as login
            .build()
        GoogleSignIn.getClient(context, gso).signOut() // Google side: forgets the cached account so the NEXT tap shows the chooser
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

    suspend fun linkEmailPassword(email: String, password: String): Result<Unit> { // glues an email+password key onto the currently signed-in Google user
        val user = auth.currentUser // the Firebase user created by the Google sign-in
        if (user == null) return Result.failure(Exception("Not signed in")) // safety net: cannot glue a key to nobody
        return try {
            val credential = EmailAuthProvider.getCredential(email, password) // turns email+password into a digital key (AuthCredential)
            user.linkWithCredential(credential).await() // THE GLUE: attaches that key to the SAME uid, no second account
            Result.success(Unit) // Unit = "job done, nothing to hand back"
        } catch (e: Exception) {
            Result.failure(e) // hand any failure up to the ViewModel
        }
    }
}
