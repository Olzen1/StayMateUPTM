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
    val accountEmail: String get() = repository.currentEmail().orEmpty()

    // computed at READ time, not build time: grabs the Google email whenever the UI asks; get() dodges the top-to-bottom build-order trap; currentEmail() needs () because it is a function; orEmpty() turns null into ""
    private val _password = MutableStateFlow("") // holds the password text as state
    val password: StateFlow<String> =
        _password.asStateFlow() // read-only copy the UI is allowed to see

    private val _confirmPassword = MutableStateFlow("") // holds the re-typed password text
    val confirmPassword: StateFlow<String> =
        _confirmPassword.asStateFlow() // read-only copy for the UI

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    } // UI calls this on every keystroke

    fun onConfirmPasswordChange(newValue: String) {
        _confirmPassword.value = newValue
    } // same, for the re-type box

    fun getPasswordErrors(): List<String> { // the bouncer: returns every broken rule (empty list = all good)
        val errors = mutableListOf<String>() // empty shopping list we fill with broken rules
        if (password.value.length < 6) errors.add("At least 6 characters") // rule 1: length
        if (!password.value.any { !it.isLetterOrDigit() }) errors.add("At least 1 special character") // rule 2: a char that is NOT letter and NOT digit
        if (!password.value.any { it.isUpperCase() }) errors.add("At least 1 uppercase letter") // rule 3: a BIG letter
        if (!password.value.any { it.isDigit() }) errors.add("At least 1 number") // rule 4: a digit
        if (confirmPassword.value.isNotEmpty() && confirmPassword.value != password.value) errors.add(
            "Passwords do not match"
        ) // match check, only once they start re-typing
        return errors // hand the list of broken rules back to the caller
    }

    private val repository = AuthRepository()

    // One StateFlow per form field = single source of truth for the UI
    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()
    fun onFullNameChange(value: String) {
        _fullName.value = value
    }

    private val _selectedCourse = MutableStateFlow("")
    val selectedCourse: StateFlow<String> = _selectedCourse.asStateFlow()
    fun onCourseSelected(value: String) {
        _selectedCourse.value = value
    }

    private val _selectedSemester = MutableStateFlow("")
    val selectedSemester: StateFlow<String> = _selectedSemester.asStateFlow()
    fun onSemesterSelected(value: String) {
        _selectedSemester.value = value
    }

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun submit() {
        val name = _fullName.value.trim() // clean the name input
        val course = _selectedCourse.value // chosen course
        val semester = _selectedSemester.value // chosen semester

        // Fail fast, client-side
        if (name.isEmpty() || course.isEmpty() || semester.isEmpty()) {
            _uiState.value = OnboardingUiState.Error("Please fill in all fields.")
            return
        }

        // BOUNCER GATE 1: all four password rules must pass before anything is saved
        val passwordProblems = getPasswordErrors() // list of broken rules (empty list = all passed)
        if (passwordProblems.isNotEmpty()) {
            _uiState.value =
                OnboardingUiState.Error("Password still needs: " + passwordProblems.joinToString(", ")) // joinToString glues the list into one sentence
            return
        }

        // BOUNCER GATE 2: re-enter box must be filled and identical (the live red list is UX; this line is the actual lock)
        if (password.value.isEmpty() || password.value != confirmPassword.value) {
            _uiState.value = OnboardingUiState.Error("Passwords do not match.")
            return
        }

        val uid = repository.currentUid() // who is signed in right now
        val email = repository.currentEmail() // their Google email
        if (uid == null || email == null) {
            _uiState.value = OnboardingUiState.Error("Session expired. Please log in again.")
            return
        }

        _uiState.value = OnboardingUiState.Saving // tell UI to show the saving state
        viewModelScope.launch { // background work, tied to this ViewModel's life
            val profile = UserProfile(
                uid = uid, fullName = name, email = email,
                course = course, semester = semester
            )
            // 1st: write the profile doc (set() overwrites, so a retry never duplicates)
            repository.createUserProfile(profile)
                .onSuccess {
                    // 2nd: THE GLUE — attach the password key to this same Google user
                    repository.linkEmailPassword(email, password.value)
                        .onSuccess {
                            _uiState.value = OnboardingUiState.Success
                        } // both jobs done → route Home
                        .onFailure { e ->
                            _uiState.value = OnboardingUiState.Error(
                                e.message ?: "Could not set password. Try again."
                            )
                        } // profile saved but key failed → stay, retry is safe
                }
                .onFailure { e ->
                    _uiState.value = OnboardingUiState.Error(e.message ?: "Save failed")
                } // profile write failed → nothing was glued, clean failure
        }
    }
}