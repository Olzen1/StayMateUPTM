package com.staymate.uptm.repository // function tells Android where this file lives

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.staymate.uptm.model.Post
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PostRepository { // function creates the Waiter class
     // read ONE post by its id, live (null if the doc is missing or deleted)
    fun observePostById(id: String): Flow<Post?> = callbackFlow {
        val listener = db.collection("posts").document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            val post = snapshot?.toObject(Post::class.java)
            trySend(post)
        }
        awaitClose { listener.remove() }
    }
    private val db = FirebaseFirestore.getInstance() // function gets the connection to our Firestore database

    // Function to save a new post to the database
    suspend fun createPost(post: Post): Result<Unit> { // function makes a suspend function that returns Success or Failure
        return try { // function starts a safety net to catch errors
            val newPostRef = db.collection("posts").document() // function creates a new empty document in the "posts" folder and gets its unique ID
            val postWithId = post.copy(id = newPostRef.id) // function copies the Post data but fills in the new unique ID
            newPostRef.set(postWithId).await() // function saves the post to Firestore and waits for it to finish

            // Record recent activity for Admin Dashboard
            val activityType = if (post.type == "group_finding") "New Group Post Added" else "New Post Added"
            val activityRef = db.collection("recent_activities").document()
            activityRef.set(mapOf(
                "id" to activityRef.id,
                "type" to activityType,
                "postTitle" to post.title,
                "timestamp" to com.google.firebase.Timestamp.now()
            ))

            Result.success(Unit) // function returns Success if it worked
        } catch (e: Exception) { // function catches any errors that happen
            Result.failure(e) // function returns Failure with the error message
        }
    }

    // Function to update an existing post
    suspend fun updatePost(post: Post): Result<Unit> {
        return try {
            db.collection("posts").document(post.id).set(post).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Function to delete a post by id
    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            db.collection("posts").document(postId).delete().await()
            // Clean up associated reports so they don't linger
            val reportsQuery = db.collection("reports").whereEqualTo("postId", postId).get().await()
            for (doc in reportsQuery.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Function to observe all reports
    fun observeReports(): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reports = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                trySend(reports)
            }
        awaitClose { listener.remove() }
    }

    // Function to dismiss (remove) every report filed against one post — the POST itself stays live
    suspend fun dismissReportsForPost(postId: String): Result<Unit> {
        return try {
            val reportsQuery = db.collection("reports").whereEqualTo("postId", postId).get().await()
            for (doc in reportsQuery.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Function to report a post
    suspend fun reportPost(postId: String, postTitle: String, reporterUid: String): Result<Unit> {
        return try {
            val reportRef = db.collection("reports").document()
            reportRef.set(mapOf(
                "id" to reportRef.id,
                "postId" to postId,
                "postTitle" to postTitle,
                "reporterUid" to reporterUid,
                "timestamp" to com.google.firebase.Timestamp.now()
            )).await()

            // Record in recent_activities for Admin
            val activityRef = db.collection("recent_activities").document()
            activityRef.set(mapOf(
                "id" to activityRef.id,
                "type" to "Post Reported",
                "postTitle" to postTitle,
                "timestamp" to com.google.firebase.Timestamp.now()
            ))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //fun observeRecentActivities(): Flow<List<Map<String, Any>>> = callbackFlow {
    //        val listener = db.collection("recent_activities")
    //            .orderBy("timestamp", Query.Direction.DESCENDING)
    //            .limit(10)
    //            .addSnapshotListener { snapshot, error ->
    //                if (error != null) {
    //                    trySend(emptyList())
    //                    return@addSnapshotListener
    //                }
    //                val activities = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
    //                trySend(activities)
    //            }
    //        awaitClose { listener.remove() }
    //    }

    fun observePosts(): Flow<List<Post>> { // function makes a Flow that spits out a List of Posts
        return callbackFlow { // function starts a special Flow builder for Firebase listeners
            val listener = db.collection("posts") // function points to the "posts" folder
                .orderBy("createdAt", Query.Direction.DESCENDING) // function sorts posts so newest is first (like Instagram)
                .addSnapshotListener { snapshot, error -> // function starts the live listener and gives us the data (snapshot) or an error
                    if (error != null) { // function checks if something went wrong
                        trySend(emptyList()) // function sends empty list instead of throwing uncaught exception
                        return@addSnapshotListener // function exits the listener early
                    }
                    if (snapshot != null) { // function checks if we actually got data
                        val posts = snapshot.toObjects(Post::class.java) // function converts the raw Firebase data into our Post blueprints
                        trySend(posts) // function pushes the list of posts down the river (Flow)
                    }
                }
            awaitClose { listener.remove() } // function cleans up and stops listening when the app no longer needs the stream
        }
    }
}