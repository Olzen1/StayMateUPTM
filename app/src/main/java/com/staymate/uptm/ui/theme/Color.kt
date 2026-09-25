package com.staymate.uptm.ui.theme

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color

val UptmBlue = Color(0xFF16519E)        // logo blue — our main action color (buttons, headers)
val UptmBlueDark = Color(0xFF0E3A6E)    // logo blue + black paint — text on bright/pale blue
val UptmBluePale = Color(0xFFD8E3F7)    // logo blue + white paint — soft blue backgrounds
val UptmBlueBright = Color(0xFFA9C7F5)  // logo blue + lots of white — plays "primary" at night
val UptmBlueNight = Color(0xFF1E4A85)   // medium-dark blue — blue boxes in dark mode

val UptmRed = Color(0xFFED1C24)         // logo red — badges & accents ONLY, never errors
val UptmRedDark = Color(0xFF8F0E14)     // logo red + black paint — text on bright red
val UptmRedPale = Color(0xFFFFDAD8)     // logo red + white paint — soft red backgrounds
val UptmRedBright = Color(0xFFFFB3AD)   // logo red + lots of white — plays "secondary" at night

val ErrorRed = Color(0xFFB3261E)        // duller cousin of logo red — mistakes ONLY
val ErrorRedPale = Color(0xFFF9DEDC)    // pale error red — gentle error box backgrounds
val ErrorRedNight = Color(0xFFF2B8B5)   // soft error red for dark mode

val UptmWhite = Color(0xFFFFFFFF)       // logo white — cards + text on blue
val PaperWhite = Color(0xFFF5F8FC)      // white with one drop of blue — page background
val InkDark = Color(0xFF171B26)         // near-black with a blue hint — main text
@SuppressLint("InvalidColorHexValue")
val NightBackground = Color(0xFF242626) // dark-mode page — navy, softer than harsh black
val NightSurface = Color(0xFF303232)    // dark-mode cards
val NightText = Color(0xFFE7EDF7)       // near-white text at night