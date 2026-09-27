// Save blueprint - one fridge magnet: who stuck it, which post, when
package com.staymate.uptm.model

import com.google.firebase.Timestamp

data class Save(
    val uid: String = "",
    val postId: String = "",
    val savedAt: Timestamp = Timestamp.now()
)