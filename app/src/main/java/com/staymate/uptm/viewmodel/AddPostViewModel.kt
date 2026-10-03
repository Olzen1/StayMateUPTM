package com.staymate.uptm.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.staymate.uptm.model.Post
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.CloudinaryRepository
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.utils.UptmConstants
import kotlinx.coroutines.Job
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
    private val authRepository: AuthRepository,
    private val cloudinaryRepository: CloudinaryRepository = CloudinaryRepository()
) : ViewModel() {
    var photoError by mutableStateOf<String?>(null)
    private val _addPostUiState = MutableStateFlow<AddPostUiState>(AddPostUiState.Idle)
    val addPostUiState: StateFlow<AddPostUiState> = _addPostUiState.asStateFlow()

    var editingPostId by mutableStateOf<String?>(null)
    var editingAuthorUid by mutableStateOf<String?>(null)
    var editingAuthorName by mutableStateOf<String?>(null)
    var editingAuthorPhotoUrl by mutableStateOf<String?>(null)
    var editingCreatedAt by mutableStateOf<com.google.firebase.Timestamp?>(null)

    // Step state: 1 = Details, 2 = Contact Information
    var currentStep by mutableIntStateOf(1)

    // Photo memory — the local Uri picked from the gallery + the Cloudinary result
    var photoUri by mutableStateOf<Uri?>(null)
    var photoUrl by mutableStateOf<String?>(null)
    var photoPublicId by mutableStateOf<String?>(null)
    var isUploadingPhoto by mutableStateOf(false)
    private var photoUploadJob: Job? = null

    // Called when the user picks (or cancels) a photo in the system photo picker
    fun onPhotoPicked(uri: Uri?) {
        photoError = null
        if (uri == null) return
        photoUri = uri
        photoUrl = null
        photoPublicId = null
        startPhotoUpload()
    }

    private fun startPhotoUpload() {
        val uri = photoUri ?: return
        photoUploadJob = viewModelScope.launch {
            isUploadingPhoto = true
            val result = cloudinaryRepository.uploadImage(uri)
            isUploadingPhoto = false
            result
                .onSuccess { upload ->
                    photoUrl = upload.secureUrl
                    photoPublicId = upload.publicId
                }
                .onFailure { error ->
                    photoError = error.message ?: "Photo upload failed. Please try again."
                }
        }
    }

    fun retryPhotoUpload() {
        if (photoUri != null && photoUrl == null && !isUploadingPhoto) {
            startPhotoUpload()
        }
    }

    fun removePhoto() {
        photoUploadJob?.cancel()
        photoUploadJob = null
        photoUri = null
        photoUrl = null
        photoPublicId = null
        isUploadingPhoto = false
        photoError = null
    }

    // Form fields - Step 1: Details
    var postType by mutableStateOf("")
    var title by mutableStateOf("")
    var propertyName by mutableStateOf("")
    var location by mutableStateOf("")
    var priceText by mutableStateOf("") // Min Price
    var maxPriceText by mutableStateOf("") // Max Price
    var description by mutableStateOf("")
    var propertyLink by mutableStateOf("")
    var selectedBedrooms by mutableStateOf("")
    var selectedPropertyType by mutableStateOf("")
    var selectedGender by mutableStateOf("")
    var selectedFacilities by mutableStateOf(listOf<String>())
    var moveInDateMillis by mutableStateOf<Long?>(null) // Ready From Date
    var moveInDateToMillis by mutableStateOf<Long?>(null) // Latest Date
    var depositText by mutableStateOf("")
    var selectedFurnished by mutableStateOf("")
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

    fun populateForEditing(post: Post) {
        editingPostId = post.id
        editingAuthorUid = post.authorUid
        editingAuthorName = post.authorName
        editingAuthorPhotoUrl = post.authorPhotoUrl
        editingCreatedAt = post.createdAt
        currentStep = 1
        postType = when (post.type) {
            UptmConstants.POST_TYPE_KEY_SUGGESTION -> UptmConstants.POST_TYPE_HOUSE_SUGGESTION
            UptmConstants.POST_TYPE_KEY_GROUP_FINDING -> UptmConstants.POST_TYPE_GROUP_FINDING
            else -> UptmConstants.POST_TYPE_HOUSEMATE_WANTED
        }
        title = post.title
        propertyName = post.propertyName
        location = post.location
        priceText = if (post.priceRM > 0) post.priceRM.toInt().toString() else ""

        description = post.description
        propertyLink = post.propertyLink
        selectedBedrooms = if (post.bedrooms > 0) post.bedrooms.toString() else ""
        selectedPropertyType = post.propertyType
        selectedGender = post.genderPreference
        selectedFurnished = post.furnishedStatus
        depositText = if (post.deposit > 0) post.deposit.toInt().toString() else ""

        currentHousematesText =
            if (post.currentHousemates > 0) post.currentHousemates.toString() else ""
        selectedFacilities = post.facilities
        moveInDateMillis = if (post.moveInDate > 0) post.moveInDate else null
        contactPhone = post.contactPhone
        contactEmail = post.contactEmail.ifBlank { authRepository.currentEmail().orEmpty() }
        contactGender = post.contactGender.ifBlank { post.genderPreference }
        contactWhatsapp = post.whatsappNumber
        contactOtherInfo = post.contactOtherInfo
        _addPostUiState.value = AddPostUiState.Idle
        photoUploadJob?.cancel()
        photoUploadJob = null
        photoUri = null
        photoError = null
        isUploadingPhoto = false
        photoUrl = post.photoUrls.firstOrNull()
    }

    fun toggleFacility(facility: String) {
        selectedFacilities = if (selectedFacilities.contains(facility)) {
            selectedFacilities - facility
        } else {
            selectedFacilities + facility
        }
    }

    fun goToNextStep(): Boolean {
        if (postType == UptmConstants.POST_TYPE_GROUP_FINDING) {
            if (title.isBlank() || selectedGender.isBlank()) {
                _addPostUiState.value = AddPostUiState.Error("Please fill in Title and Gender.")
                return false
            }
        } else {
            if (title.isBlank() || propertyName.isBlank() || location.isBlank() || priceText.isBlank() || selectedBedrooms.isBlank() || selectedPropertyType.isBlank()) {
                _addPostUiState.value =
                    AddPostUiState.Error("Please fill in all required fields in Details (Title, Property Name, Location, Price, Bedrooms, Property Type).")
                return false
            }
            if (postType == UptmConstants.POST_TYPE_HOUSEMATE_WANTED && selectedGender.isBlank()) {
                _addPostUiState.value =
                    AddPostUiState.Error("Please choose a preferred housemate gender.")
                return false
            }
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
            if (postType == UptmConstants.POST_TYPE_GROUP_FINDING) {
                if (title.isBlank() || selectedGender.isBlank()) {
                    currentStep = 1
                    _addPostUiState.value = AddPostUiState.Error("Please fill in Title and Gender.")
                    return@launch
                }
            } else {
                if (title.isBlank() || propertyName.isBlank() || location.isBlank() || priceText.isBlank() || selectedBedrooms.isBlank() || selectedPropertyType.isBlank()) {
                    currentStep = 1
                    _addPostUiState.value =
                        AddPostUiState.Error("Please fill in required fields in Details.")
                    return@launch
                }
            }

            if (contactPhone.isBlank() || contactGender.isBlank()) {
                currentStep = 2
                _addPostUiState.value =
                    AddPostUiState.Error("Please fill in required Contact Information (Phone Number and Gender).")
                return@launch
            }

            // If a photo upload is still running, wait for it to finish.
            // The photo is optional — if the upload failed we simply save the post without it.
            photoUploadJob?.join()

            _addPostUiState.value = AddPostUiState.Saving
            val uid = authRepository.currentUid()
            if (uid == null) {
                _addPostUiState.value =
                    AddPostUiState.Error("You are not logged in anymore. Please login again.")
                return@launch
            }

            val profile = authRepository.observeUserProfile(uid).first()
            val authorName = profile?.fullName ?: "StayMate User"
            val typeKey = when (postType) {
                UptmConstants.POST_TYPE_HOUSE_SUGGESTION -> UptmConstants.POST_TYPE_KEY_SUGGESTION
                UptmConstants.POST_TYPE_GROUP_FINDING -> UptmConstants.POST_TYPE_KEY_GROUP_FINDING
                else -> UptmConstants.POST_TYPE_KEY_HOUSEMATE
            }

            val post = Post(
                id = editingPostId ?: "",
                authorUid = if (editingPostId != null && !editingAuthorUid.isNullOrBlank()) editingAuthorUid!! else uid,
                authorName = if (editingPostId != null && !editingAuthorName.isNullOrBlank()) editingAuthorName!! else authorName,
                authorPhotoUrl = if (editingPostId != null && editingAuthorPhotoUrl != null) editingAuthorPhotoUrl!! else (profile?.photoUrl ?: ""),
                type = typeKey,
                title = title,
                genderPreference = selectedGender,
                propertyName = propertyName,
                location = location,
                priceRM = priceText.toDoubleOrNull() ?: 0.0,
                bedrooms = selectedBedrooms.toLongOrNull() ?: 0,
                deposit = depositText.toDoubleOrNull() ?: 0.0,
                furnishedStatus = selectedFurnished,

                currentHousemates = currentHousematesText.toLongOrNull() ?: 0,
                propertyType = selectedPropertyType,
                propertyLink = if (typeKey == UptmConstants.POST_TYPE_KEY_SUGGESTION) propertyLink else "",
                facilities = selectedFacilities,
                moveInDate = moveInDateMillis ?: 0,
                createdAt = editingCreatedAt ?: com.google.firebase.Timestamp.now(),
                description = description,
                contactPhone = contactPhone.trim(),
                contactEmail = contactEmail.trim()
                    .ifBlank { authRepository.currentEmail().orEmpty() },
                contactGender = contactGender,
                whatsappNumber = contactWhatsapp.trim().ifBlank { contactPhone.trim() },
                contactOtherInfo = contactOtherInfo.trim(),
                photoUrls = listOfNotNull(photoUrl),   // the photo finally travels into the doc
            )

            val result = if (editingPostId != null) {
                postRepository.updatePost(post)
            } else {
                postRepository.createPost(post)
            }

            result.onSuccess {
                _addPostUiState.value = AddPostUiState.Success
            }.onFailure { error ->
                _addPostUiState.value =
                    AddPostUiState.Error(error.message ?: "Could not save the post. Try again.")
            }
        }
    }

    fun resetForm() {
        editingPostId = null
        editingAuthorUid = null
        editingAuthorName = null
        editingAuthorPhotoUrl = null
        editingCreatedAt = null
        currentStep = 1
        title = ""
        propertyName = ""
        location = ""
        priceText = ""
        maxPriceText = ""
        description = ""
        propertyLink = ""
        selectedBedrooms = ""
        selectedPropertyType = ""
        selectedGender = ""
        selectedFurnished = ""
        depositText = ""
        currentHousematesText = ""
        selectedFacilities = emptyList()
        moveInDateMillis = null
        moveInDateToMillis = null
        contactPhone = ""
        contactEmail = authRepository.currentEmail().orEmpty()
        contactGender = ""
        contactWhatsapp = ""
        contactOtherInfo = ""
        _addPostUiState.value = AddPostUiState.Idle
        photoUploadJob?.cancel()
        photoUploadJob = null
        photoUri = null
        photoUrl = null
        isUploadingPhoto = false
        photoPublicId = null
        photoError = null
    }
}