package com.staymate.uptm.repository // function tells Android where this file lives

import com.google.firebase.firestore.FirebaseFirestore
import com.staymate.uptm.model.Post
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
class PostRepository { // function creates the Waiter class
    // read ONE post by its id, live (null if the doc is missing or deleted)
    fun observePostById(id: String): Flow<Post?> = callbackFlow {
        // use the SAME firestore handle your observePosts uses on its first line.
        // I wrote 'db' below — if your field is named differently, swap it to match exactly.
        val listener = db.collection("posts").document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {            // the stream itself broke (rules / offline) -> hand the error upward
                close(error)
                return@addSnapshotListener
            }
            val post = snapshot?.toObject(Post::class.java)   // null when that doc simply doesn't exist
            trySend(post)                   // push the one post (or null) to whoever is listening
        }
        awaitClose { listener.remove() }    // screen leaves -> unplug the listener (no leak)
    }
    private val db = FirebaseFirestore.getInstance() // function gets the connection to our Firestore database

    // Function to save a new post to the database
    suspend fun createPost(post: Post): Result<Unit> { // function makes a suspend function that returns Success or Failure
        return try { // function starts a safety net to catch errors
            val newPostRef = db.collection("posts").document() // function creates a new empty document in the "posts" folder and gets its unique ID
            val postWithId = post.copy(id = newPostRef.id) // function copies the Post data but fills in the new unique ID
            newPostRef.set(postWithId).await() // function saves the post to Firestore and waits for it to finish
            Result.success(Unit) // function returns Success if it worked
        } catch (e: Exception) { // function catches any errors that happen
            Result.failure(e) // function returns Failure with the error message
        }
    }

    fun observePosts(): Flow<List<Post>> { // function makes a Flow that spits out a List of Posts
        return callbackFlow { // function starts a special Flow builder for Firebase listeners
            val listener = db.collection("posts") // function points to the "posts" folder
                .orderBy("createdAt", Query.Direction.DESCENDING) // function sorts posts so newest is first (like Instagram)
                .addSnapshotListener { snapshot, error -> // function starts the live listener and gives us the data (snapshot) or an error
                    if (error != null) { // function checks if something went wrong
                        close(error) // function stops the stream and sends the error
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