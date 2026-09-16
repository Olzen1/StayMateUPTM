package com.staymate.uptm.viewmodel // function tells Android where this file lives

import android.net.Uri
import androidx.compose.remote.creation.dsl.first
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel // function imports the base ViewModel class
import androidx.lifecycle.viewModelScope // function imports the tool to run background tasks
import com.staymate.uptm.repository.PostRepository // function imports our Repository
import com.staymate.uptm.model.Post // function imports our Post blueprint
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.utils.UptmConstants
import kotlinx.coroutines.flow.MutableStateFlow // function imports the tank for UI state
import kotlinx.coroutines.flow.StateFlow // function imports the read-only tank
import kotlinx.coroutines.flow.asStateFlow // function converts private to public
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch // function starts background tasks

// function defines the possible states of the Add Post form
sealed interface AddPostUiState { // function creates a sealed interface for form states
    data object Idle : AddPostUiState // function means form is ready for input
    data object Saving : AddPostUiState // function means we are saving the post
    data object Success : AddPostUiState // function means post was created successfully
    data class Error(val message: String) : AddPostUiState // function means something went wrong
}


class AddPostViewModel(
    private val postRepository: PostRepository, // function receives the posts waiter
    private val authRepository: AuthRepository // function receives the login waiter so we can stamp the author
) : ViewModel() {

    // function holds the current UI state
    private val _addPostUiState = MutableStateFlow<AddPostUiState>(AddPostUiState.Idle) // function starts with Idle state
    val addPostUiState: StateFlow<AddPostUiState> = _addPostUiState.asStateFlow() // function exposes read-only state

    // function holds the form field values (we'll add more fields as we build the form)
    var postType by mutableStateOf("") // function holds the type of post being created (House Suggestion or Housemate Wanted)
    var title by mutableStateOf("") // function holds the post title text
    var selectedPhotoUris by mutableStateOf<List<Uri>>(emptyList()) // function holds the list of picked photos (empty at start)

    var propertyLink by mutableStateOf("") // function holds the property website link the user types
    fun addPhotos(uris: List<Uri>) { // function receives the photos the user just picked
        selectedPhotoUris = (selectedPhotoUris + uris).take(6) // function joins old + new then chops everything past 6 (wireframe limit)
    }

    fun removePhoto(uri: Uri) { // function receives the photo to delete
        selectedPhotoUris = selectedPhotoUris - uri // function drops that one photo from the list
    }
    var propertyName by mutableStateOf("") // function holds the House/Property Name text
    var location by mutableStateOf("") // function holds the address text
    var priceText by mutableStateOf("") // function holds the price as TEXT (we convert to a number only when posting)
    var description by mutableStateOf("") // function holds the About Us text
    var selectedBedrooms by mutableStateOf("") // function holds the bedrooms dropdown choice (empty = not chosen yet)
    var selectedPropertyType by mutableStateOf("") // function holds the property type dropdown choice
    var selectedGender by mutableStateOf("") // function holds the gender preference dropdown choice
    var selectedFacilities by mutableStateOf(listOf<String>()) // function holds the list of tapped facility chips
    var moveInDateMillis by mutableStateOf<Long?>(null) // function holds the picked date as milliseconds (null = not picked yet)

    // function flips a facility chip on/off when tapped
    fun toggleFacility(facility: String) { // function receives the tapped facility name
        selectedFacilities = if (selectedFacilities.contains(facility)) { // function checks if it is already picked
            selectedFacilities - facility // function removes it (untapping a chip)
        } else { // function means it was not picked
            selectedFacilities + facility // function adds it (tapping a chip)
        }
    }
    // function to create a new post
    fun createPost() { // function gathers every field and ships the post to Firestore
        viewModelScope.launch { // function runs the save in the background so the UI never freezes
            if (title.isBlank() || propertyName.isBlank() || location.isBlank() || priceText.isBlank() || selectedBedrooms.isBlank() || selectedPropertyType.isBlank()) { // function gates the six always-required fields
                _addPostUiState.value = AddPostUiState.Error("Please fill in title, property name, location, price, bedrooms and property type.") // function shows a friendly reminder in red
                return@launch // function stops here so a half-empty post is never saved
            }
            if (postType == UptmConstants.POST_TYPE_HOUSEMATE_WANTED && selectedGender.isBlank()) { // function adds one extra gate only for housemate posts
                _addPostUiState.value = AddPostUiState.Error("Please choose a preferred housemate gender.") // function shows a friendly reminder
                return@launch // function stops here
            }
            _addPostUiState.value = AddPostUiState.Saving // function flips state so the button shows a spinner and freezes
            val uid = authRepository.currentUid() // function asks Auth who is logged in right now
            if (uid == null) { // function checks login somehow vanished mid-form
                _addPostUiState.value = AddPostUiState.Error("You are not logged in anymore. Please login again.") // function explains the problem
                return@launch // function stops here
            }
            val profile = authRepository.observeUserProfile(uid).first() // function takes ONE sip of the profile river just to read the name
            val authorName = profile?.fullName ?: "StayMate User" // function uses the real name, or a fallback if the profile doc is missing
            val typeKey = if (postType == UptmConstants.POST_TYPE_HOUSE_SUGGESTION) UptmConstants.POST_TYPE_KEY_SUGGESTION else UptmConstants.POST_TYPE_KEY_HOUSEMATE // function translates the pretty label into the locked database key
            val post = Post( // function builds the complete post blueprint from every form field
                authorUid = uid, // function stamps the owner id
                authorName = authorName, // function stamps the denormalized name copy (brainstorm answer 5)
                type = typeKey, // function stamps the database key
                title = title, // function copies the typed title
                genderPreference = if (typeKey == UptmConstants.POST_TYPE_KEY_HOUSEMATE) selectedGender else "", // function keeps gender only on housemate posts
                propertyName = propertyName, // function copies the house name
                location = location, // function copies the address
                priceRM = priceText.toDoubleOrNull() ?: 0.0, // function turns "293.42" text into a real number safely (null becomes 0.0)
                bedrooms = selectedBedrooms.toLongOrNull() ?: 0, // function turns "3" text into a whole number safely
                propertyType = selectedPropertyType, // function copies the dropdown pick
                propertyLink = if (typeKey == UptmConstants.POST_TYPE_KEY_SUGGESTION) propertyLink else "", // function keeps the link only on suggestion posts
                facilities = selectedFacilities, // function copies the tapped chips list
                moveInDate = if (typeKey == UptmConstants.POST_TYPE_KEY_HOUSEMATE) moveInDateMillis ?: 0 else 0, // function keeps the date only on housemate posts
                description = if (typeKey == UptmConstants.POST_TYPE_KEY_HOUSEMATE) description else "", // function keeps About Us only on housemate posts
                photoUrls = emptyList() // function leaves photos empty until Sprint 3 Storage upload fills them
            )
            postRepository.createPost(post).onSuccess { // function ships it and listens for success
                _addPostUiState.value = AddPostUiState.Success // function flips state so the screen bounces home
            }.onFailure { error -> // function listens for failure
                _addPostUiState.value = AddPostUiState.Error(error.message ?: "Could not save the post. Try again.") // function shows what broke
            }
        }
    }
    fun resetForm() { // function empties every field so the next visit starts fresh
        title = "" // function clears the title
        propertyName = "" // function clears the house name
        location = "" // function clears the address
        priceText = "" // function clears the price
        description = "" // function clears About Us
        propertyLink = "" // function clears the link
        selectedBedrooms = "" // function clears the bedrooms pick
        selectedPropertyType = "" // function clears the property type pick
        selectedGender = "" // function clears the gender pick
        selectedFacilities = emptyList() // function clears the chips
        selectedPhotoUris = emptyList() // function clears the photos
        moveInDateMillis = null // function clears the date
        _addPostUiState.value = AddPostUiState.Idle // function resets the state tank back to Idle
    }
}