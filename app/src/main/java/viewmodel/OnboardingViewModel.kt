package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.staymate.uptm.model.UserProfile
import com.staymate.uptm.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OnboardingUiState {
    object Idle : OnboardingUiState()
    object Saving : OnboardingUiState()
    object Success : OnboardingUiState()
    data class Error(val message: String) : OnboardingUiState()
}

class OnboardingViewModel : ViewModel() {
    private val repository = AuthRepository()

    // One StateFlow per form field = single source of truth for the UI
    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()
    fun onFullNameChange(value: String) { _fullName.value = value }

    private val _selectedCourse = MutableStateFlow("")
    val selectedCourse: StateFlow<String> = _selectedCourse.asStateFlow()
    fun onCourseSelected(value: String) { _selectedCourse.value = value }

    private val _selectedSemester = MutableStateFlow("")
    val selectedSemester: StateFlow<String> = _selectedSemester.asStateFlow()
    fun onSemesterSelected(value: String) { _selectedSemester.value = value }

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun submit() {
        val name = _fullName.value.trim()
        val course = _selectedCourse.value
        val semester = _selectedSemester.value

        // Fail fast, client-side
        if (name.isEmpty() || course.isEmpty() || semester.isEmpty()) {
            _uiState.value = OnboardingUiState.Error("Please fill in all fields.")
            return
        }
        val uid = repository.currentUid()
        val email = repository.currentEmail()
        if (uid == null || email == null) {
            _uiState.value = OnboardingUiState.Error("Session expired. Please log in again.")
            return
        }

        _uiState.value = OnboardingUiState.Saving
        viewModelScope.launch {
            val profile = UserProfile(
                uid = uid, fullName = name, email = email,
                course = course, semester = semester
            )
            repository.createUserProfile(profile)
                .onSuccess { _uiState.value = OnboardingUiState.Success }
                .onFailure { e -> _uiState.value = OnboardingUiState.Error(e.message ?: "Save failed") }
        }
    }
}