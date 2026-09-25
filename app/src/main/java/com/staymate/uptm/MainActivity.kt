package com.staymate.uptm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.staymate.uptm.ui.theme.StayMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StayMateTheme {  // WEAR the UPTM uniform (was: MaterialTheme = factory default, always purple, ignores dark mode)
                RootScreen()
            }
        }
    }
}