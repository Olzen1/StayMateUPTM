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
@Suppress("DEPRECATION")
class AuthRepository {


    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun isUptmEmail(email: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty()) return false
        val domain = cleanEmail.substringAfter("@", "")
        return domain == "student.uptm.edu.my" ||
               domain == "uptm.edu.my" ||
               domain == "gapps.uptm.edu.my" ||
               domain == "admin.uptm.edu.my" ||
               domain.endsWith(".uptm.edu.my")
    }

    fun isAdminEmail(email: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty()) return false
        return cleanEmail == "admin@admin.uptm.edu.my" ||
               cleanEmail.startsWith("admin@") ||
               cleanEmail.endsWith("@admin.uptm.edu.my")
    }
    suspend fun signInWithEmail(email: String, password: String): Result<String> { // sign-in ONLY: no auto-create anymore, registration lives behind the Google door now
        return try {
            val cleanEmail = email.trim()
            // admin login — never hand back a fake badge; only a REAL session, or an honest failure
            // admin login — the app holds NO copy of the password; Firebase Auth checks it server-side
            if (cleanEmail.equals("admin@admin.uptm.edu.my", ignoreCase = true)) {
                return try {
                    auth.signInWithEmailAndPassword(cleanEmail, password).await()
                    val realUid = auth.currentUser?.uid
                    if (realUid != null) Result.success(realUid)
                    else Result.failure(Exception("Admin session did not stick. Try again."))
                } catch (e: Exception) {
                    // wrong password / no such account / network: ONE honest failure, no fake badge
                    Result.failure(Exception("Wrong admin email or password."))
                }
            }

            if (!isUptmEmail(cleanEmail)) {
                return Result.failure(Exception("Please use a valid UPTM student email (@student.uptm.edu.my)."))
            }
            auth.signInWithEmailAndPassword(cleanEmail, password).await() // try the existing account
            val user = auth.currentUser
            if (user != null && isUptmEmail(user.email.orEmpty())) {
                Result.success(user.uid)
            } else {
                auth.signOut()
                Result.failure(Exception("Please use a valid UPTM student email (@student.uptm.edu.my)."))
            }
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
            if (user != null && isUptmEmail(user.email.orEmpty())) {
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
    // update ONLY the editable lines of a user's profile doc
// we use update() (a correction pen), NOT set() (a fresh photocopy),
// so we never accidentally erase email / createdAt / uid that this dialog never sends
    suspend fun updateUserProfile(
        uid: String,
        fullName: String,
        course: String,
        semester: String
    ): Result<Unit> {
        return try {
            // build a tiny map holding just the three changed values
            val changes = mapOf(
                "fullName" to fullName,
                "course" to course,
                "semester" to semester
            )
            // write that map onto the existing doc (partial overwrite)
            firestore.collection("users").document(uid).update(changes).await()
            // job done, nothing to hand back (same "Unit" token as linkEmailPassword)
            Result.success(Unit)
        } catch (e: Exception) {
            // hand any failure up to the ViewModel
            Result.failure(e)
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
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(UserProfile::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun observeAllUsers(): Flow<List<UserProfile>> = callbackFlow {
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val usersList = snapshot?.toObjects(UserProfile::class.java) ?: emptyList()
                trySend(usersList)
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
