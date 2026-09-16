package com.staymate.uptm.model // function tells Android where this file lives

import com.google.firebase.Timestamp // function imports Firebase time tool

data class Post( // function creates a blueprint for a Post
    val id: String = "", // function holds the unique ID of the post
    val authorUid: String = "", // function holds the ID of the user who posted it
    val authorName: String = "", // function holds the name of the user (copied so we don't have to look it up later)
    val type: String = "", // function holds "House Suggestion" or "Housemate Wanted"
    val title: String = "", // function holds the manual title user typed
    val genderPreference: String = "", // function holds "Male" or "Female"
    val propertyName: String = "", // function holds the house name
    val location: String = "", // function holds the address
    val priceRM: Double = 0.0, // function holds the price in Ringgit WITH cents
    val bedrooms: Long = 0, // function holds number of rooms
    val propertyType: String = "", // function holds Studio/Condo/etc.
    val propertyLink: String = "", // function holds the outside website link (PropertyGuru, iProperty, etc); stays empty for housemate posts
    val facilities: List<String> = emptyList(), // function holds the list of selected facilities
    val moveInDate: Long = 0, // function holds the date as a timestamp number
    val description: String = "", // function holds the "About Us" text
    val photoUrls: List<String> = emptyList(), // function holds the list of photo links (empty for now)
    val createdAt: Timestamp = Timestamp.now(), // function holds the exact time it was posted
    val likeCount: Long = 0, // function holds how many likes it has
    val commentCount: Long = 0 // function holds how many comments it has
)