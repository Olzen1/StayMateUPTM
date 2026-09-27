// SaveRepository - the hands that stick and peel magnets in the saves collection
package com.staymate.uptm.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.staymate.uptm.model.Save
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SaveRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // live stream of every post id this user has magnet-ed
    fun observeSavedPostIds(uid: String): Flow<Set<String>> = callbackFlow {
        val listener = firestore.collection("saves")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val saves = snapshot?.toObjects(Save::class.java) ?: emptyList()
                trySend(saves.map { it.postId }.toSet())
            }
        awaitClose { listener.remove() }
    }

    // stick one magnet; doc id = uid_postId so a double-tap can never double-save
    suspend fun savePost(uid: String, postId: String): Result<Unit> = try {
        firestore.collection("saves")
            .document("${uid}_$postId")
            .set(Save(uid = uid, postId = postId))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // peel one magnet off
    suspend fun unsavePost(uid: String, postId: String): Result<Unit> = try {
        firestore.collection("saves")
            .document("${uid}_$postId")
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}