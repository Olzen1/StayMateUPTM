package com.staymate.uptm.model

import com.google.firebase.Timestamp

data class Post(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val type: String = "",
    val title: String = "",
    val genderPreference: String = "",
    val propertyName: String = "",
    val location: String = "",
    val priceRM: Double = 0.0,
    val bedrooms: Long = 0,
    val propertyType: String = "",
    val propertyLink: String = "",
    val facilities: List<String> = emptyList(),
    val moveInDate: Long = 0,
    val description: String = "",
    val photoUrls: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val likeCount: Long = 0,
    val deposit: Double = 0.0,
    val furnishedStatus: String = "",

    val currentHousemates: Long = 0,
    val commentCount: Long = 0,
    val contactPhone: String = "",
    val contactEmail: String = "",
    val contactGender: String = "",
    val whatsappNumber: String = "",
    val contactOtherInfo: String = ""
)
