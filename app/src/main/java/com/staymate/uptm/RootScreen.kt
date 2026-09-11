package com.staymate.uptm
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.staymate.uptm.viewmodel.RootViewModel
import com.staymate.uptm.viewmodel.StartupState

    @Composable
    fun RootScreen(viewModel: RootViewModel = viewModel()) {
        val state by viewModel.startupState.collectAsStateWithLifecycle()

        when (state) {
            StartupState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            StartupState.NotSignedIn -> LoginScreen(onLoginSuccess = { viewModel.checkStartup() })
            StartupState.NeedsOnboarding -> OnboardingScreen(onComplete = { viewModel.checkStartup() })
            StartupState.Ready -> MainScreen()
        }
    }
