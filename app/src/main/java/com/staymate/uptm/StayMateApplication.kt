package com.staymate.uptm

import android.app.Application
import com.cloudinary.android.MediaManager
import com.staymate.uptm.repository.CloudinaryRepository
import com.staymate.uptm.utils.PostNotificationHelper

class StayMateApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = mapOf(
            "cloud_name" to CloudinaryRepository.CLOUD_NAME,
        )
        MediaManager.init(this, config)

        PostNotificationHelper.createNotificationChannel(this)
    }
}
