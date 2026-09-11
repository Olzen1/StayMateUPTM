package com.staymate.uptm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.staymate.uptm.ui.theme.StayMateUPTMTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("LoginActivity", "onCreate started")
        super.onCreate(savedInstanceState)
        Log.d("LoginActivity", "super.onCreate finished")
        enableEdgeToEdge()
        setContent {
            Log.d("LoginActivity", "setContent block started")
            StayMateUPTMTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    LoginScreen(
                        onLoginSuccess = {
                            Log.d("LoginActivity", "Login successful, navigating to MainActivity")
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
        Log.d("LoginActivity", "onCreate finished")
    }
}