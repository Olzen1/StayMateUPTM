package com.staymate.uptm.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume

// What a successful upload hands back: the https delivery URL (stored in Firestore)
// plus the Cloudinary public id (handy for future asset cleanup).
data class CloudinaryUploadResult(
    val secureUrl: String,
    val publicId: String
)

class CloudinaryRepository {

    // Uploads the image at [imageUri] to Cloudinary using the UNSIGNED upload preset —
    // no API secret is shipped with the app, the preset does all the signing.
    suspend fun uploadImage(imageUri: Uri): Result<CloudinaryUploadResult> =
        suspendCancellableCoroutine { continuation ->
            MediaManager.get().upload(imageUri)
                .unsigned(UPLOAD_PRESET)
                .option("folder", UPLOAD_FOLDER)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val url = (resultData?.get("secure_url") as? String).orEmpty()
                        val publicId = (resultData?.get("public_id") as? String).orEmpty()
                        if (url.isBlank()) {
                            continuation.resume(
                                Result.failure(IOException("Upload finished but no URL was returned"))
                            )
                        } else {
                            continuation.resume(Result.success(CloudinaryUploadResult(url, publicId)))
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        continuation.resume(
                            Result.failure(
                                IOException(error?.description ?: "Photo upload failed")
                            )
                        )
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        // Network hiccup — the SDK schedules a retry automatically
                    }
                })
                .dispatch()
        }

    companion object {
        const val CLOUD_NAME = "yuwfqffe"        // used by StayMateApplication to init the SDK
        const val UPLOAD_PRESET = "d6o5qko7"     // unsigned upload preset
        private const val UPLOAD_FOLDER = "staymate_posts"
    }
}