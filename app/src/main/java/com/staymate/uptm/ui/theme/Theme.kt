package com.staymate.uptm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val UptmLightColors = lightColorScheme(   // the day uniform: white pages, strong blue actions
    primary = UptmBlue,                    // big buttons (Continue, Post), active stuff
    onPrimary = UptmWhite,                 // text sitting on those blue buttons
    primaryContainer = UptmBluePale,       // selected chips, soft blue boxes
    onPrimaryContainer = UptmBlueDark,     // text inside those soft blue boxes
    secondary = UptmRed,                   // type badges, notification dot, red accents
    onSecondary = UptmWhite,               // text on red badges
    secondaryContainer = UptmRedPale,      // soft red boxes
    onSecondaryContainer = UptmRedDark,    // text inside soft red boxes
    tertiary = UptmBlueDark,               // spare blue for variety later
    background = PaperWhite,               // the page behind everything
    onBackground = InkDark,                // normal page text
    surface = UptmWhite,                   // cards float on pure white
    onSurface = InkDark,                   // text on those cards
    error = ErrorRed,                      // mistakes only — deliberately NOT UptmRed
    onError = UptmWhite,                   // text on error red
    errorContainer = ErrorRedPale,         // pale background for error boxes
    onErrorContainer = UptmRedDark         // text inside error boxes
)

private val UptmDarkColors = darkColorScheme(    // the night uniform: navy pages, glowing colors
    primary = UptmBlueBright,              // blue lightened so it still pops in the dark
    onPrimary = UptmBlueDark,              // dark text on that bright blue
    primaryContainer = UptmBlueNight,      // blue boxes at night
    onPrimaryContainer = UptmBluePale,     // pale text inside them
    secondary = UptmRedBright,             // red lightened for the dark
    onSecondary = UptmRedDark,             // dark text on bright red
    secondaryContainer = UptmRedDark,      // deep red boxes at night
    onSecondaryContainer = UptmRedPale,    // pale text inside them
    tertiary = UptmBluePale,               // spare pale blue at night
    background = NightBackground,          // navy page, not harsh black
    onBackground = NightText,              // near-white text
    surface = NightSurface,                // night cards, slightly lighter than the page
    onSurface = NightText,                 // text on night cards
    error = ErrorRedNight,                 // soft error red for dark mode
    onError = UptmRedDark                  // dark text on it
)

@Composable
fun StayMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),  // Step 1: obey the phone's light/dark setting; the Settings toggle comes in Step 4
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) UptmDarkColors else UptmLightColors  // pick today's uniform
    MaterialTheme(
        colorScheme = colors,             // every screen reads its colors from this notice board
        typography = Typography,          // fonts unchanged — Type.kt still in charge
        content = content                 // the actual screens
    )
}