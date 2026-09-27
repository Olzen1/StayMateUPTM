package com.staymate.uptm.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.staymate.uptm.model.Post
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.utils.UptmConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface AddPostUiState {
    data object Idle : AddPostUiState
    data object Saving : AddPostUiState
    data object Success : AddPostUiState
    data class Error(val message: String) : AddPostUiState
}

class AddPostViewModel(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _addPostUiState = MutableStateFlow<AddPostUiState>(AddPostUiState.Idle)
    val addPostUiState: StateFlow<AddPostUiState> = _addPostUiState.asStateFlow()

    // Step state: 1 = Details, 2 = Contact Information
    var currentStep by mutableIntStateOf(1)

    // Form fields - Step 1: Details
    var postType by mutableStateOf("")
    var title by mutableStateOf("")
    var propertyName by mutableStateOf("")
    var location by mutableStateOf("")
    var priceText by mutableStateOf("")
    var description by mutableStateOf("")
    var propertyLink by mutableStateOf("")
    var selectedBedrooms by mutableStateOf("")
    var selectedPropertyType by mutableStateOf("")
    var selectedGender by mutableStateOf("")
    var selectedFacilities by mutableStateOf(listOf<String>())
    var moveInDateMillis by mutableStateOf<Long?>(null)
    var depositText by mutableStateOf("")
    var selectedFurnished by mutableStateOf("")
    var rentPerPersonText by mutableStateOf("")
    var currentHousematesText by mutableStateOf("")

    // Form fields - Step 2: Contact Information
    var contactPhone by mutableStateOf("")
    var contactEmail by mutableStateOf("")
    var contactGender by mutableStateOf("")
    var contactWhatsapp by mutableStateOf("")
    var contactOtherInfo by mutableStateOf("")

    init {
        contactEmail = authRepository.currentEmail().orEmpty()
    }

    fun toggleFacility(facility: String) {
        selectedFacilities = if (selectedFacilities.contains(facility)) {
            selectedFacilities - facility
        } else {
            selectedFacilities + facility
        }
    }

    fun goToNextStep(): Boolean {
        if (title.isBlank() || propertyName.isBlank() || location.isBlank() || priceText.isBlank() || selectedBedrooms.isBlank() || selectedPropertyType.isBlank()) {
            _addPostUiState.value = AddPostUiState.Error("Please fill in all required fields in Details (Title, Property Name, Location, Price, Bedrooms, Property Type).")
            return false
        }
        if (postType == UptmConstants.POST_TYPE_HOUSEMATE_WANTED && selectedGender.isBlank()) {
            _addPostUiState.value = AddPostUiState.Error("Please choose a preferred housemate gender.")
            return false
        }
        _addPostUiState.value = AddPostUiState.Idle
        currentStep = 2
        return true
    }

    fun goToPreviousStep() {
        _addPostUiState.value = AddPostUiState.Idle
        currentStep = 1
    }

    fun createPost() {
        viewModelScope.launch {
            if (title.isBlank() || propertyName.isBlank() || location.isBlank() || priceText.isBlank() || selectedBedrooms.isBlank() || selectedPropertyType.isBlank()) {
                currentStep = 1
                _addPostUiState.value = AddPostUiState.Error("Please fill in required fields in Details.")
                return@launch
            }
            if (contactPhone.isBlank() || contactGender.isBlank()) {
                currentStep = 2
                _addPostUiState.value = AddPostUiState.Error("Please fill in required Contact Information (Phone Number and Gender).")
                return@launch
            }

            _addPostUiState.value = AddPostUiState.Saving
            val uid = authRepository.currentUid()
            if (uid == null) {
                _addPostUiState.value = AddPostUiState.Error("You are not logged in anymore. Please login again.")
                return@launch
            }

            val profile = authRepository.observeUserProfile(uid).first()
            val authorName = profile?.fullName ?: "StayMate User"
            val typeKey = if (postType == UptmConstants.POST_TYPE_HOUSE_SUGGESTION) UptmConstants.POST_TYPE_KEY_SUGGESTION else UptmConstants.POST_TYPE_KEY_HOUSEMATE

            val post = Post(
                authorUid = uid,
                authorName = authorName,
                authorPhotoUrl = profile?.photoUrl ?: "",
                type = typeKey,
                title = title,
                genderPreference = if (typeKey == UptmConstants.POST_TYPE_KEY_HOUSEMATE) selectedGender else "",
                propertyName = propertyName,
                location = location,
                priceRM = priceText.toDoubleOrNull() ?: 0.0,
                bedrooms = selectedBedrooms.toLongOrNull() ?: 0,
                deposit = depositText.toDoubleOrNull() ?: 0.0,
                furnishedStatus = selectedFurnished,
                rentPerPerson = rentPerPersonText.toDoubleOrNull() ?: 0.0,
                currentHousemates = currentHousematesText.toLongOrNull() ?: 0,
                propertyType = selectedPropertyType,
                propertyLink = if (typeKey == UptmConstants.POST_TYPE_KEY_SUGGESTION) propertyLink else "",
                facilities = selectedFacilities,
                moveInDate = if (typeKey == UptmConstants.POST_TYPE_KEY_HOUSEMATE) moveInDateMillis ?: 0 else 0,
                description = if (typeKey == UptmConstants.POST_TYPE_KEY_HOUSEMATE) description else "",
                contactPhone = contactPhone.trim(),
                contactEmail = contactEmail.trim().ifBlank { authRepository.currentEmail().orEmpty() },
                contactGender = contactGender,
                whatsappNumber = contactWhatsapp.trim().ifBlank { contactPhone.trim() },
                contactOtherInfo = contactOtherInfo.trim()
            )

            postRepository.createPost(post).onSuccess {
                _addPostUiState.value = AddPostUiState.Success
            }.onFailure { error ->
                _addPostUiState.value = AddPostUiState.Error(error.message ?: "Could not save the post. Try again.")
            }
        }
    }

    fun resetForm() {
        currentStep = 1
        title = ""
        propertyName = ""
        location = ""
        priceText = ""
        description = ""
        propertyLink = ""
        selectedBedrooms = ""
        selectedPropertyType = ""
        selectedGender = ""
        selectedFurnished = ""
        rentPerPersonText = ""
        depositText = ""
        currentHousematesText = ""
        selectedFacilities = emptyList()
        moveInDateMillis = null
        contactPhone = ""
        contactEmail = authRepository.currentEmail().orEmpty()
        contactGender = ""
        contactWhatsapp = ""
        contactOtherInfo = ""
        _addPostUiState.value = AddPostUiState.Idle
    }
}
