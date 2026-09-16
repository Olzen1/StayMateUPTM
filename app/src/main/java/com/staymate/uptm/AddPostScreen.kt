package com.staymate.uptm
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.AddPostViewModel
import com.staymate.uptm.viewmodel.AddPostUiState
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import java.text.SimpleDateFormat
import androidx.compose.material3.DatePicker // function imports the calendar grid
import androidx.compose.material3.DatePickerDialog // function imports the calendar pop-up frame
import androidx.compose.material3.OutlinedButton // function imports the bordered button
import androidx.compose.material3.TextButton // function imports the plain text button
import androidx.compose.material3.rememberDatePickerState // function imports the calendar memory holder
import java.util.Date // function imports the millis-to-date converter
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(
    addPostViewModel: AddPostViewModel, // function receives the ViewModel
    postType: String, // function receives the chosen post type from MainScreen
    onNavigateBack: () -> Unit, // function callback to go back
    onPostSuccess: () -> Unit // function callback when post is created
) {
    LaunchedEffect(postType) { // function runs once when the screen gets the post type
        addPostViewModel.postType = postType // function saves the type into the ViewModel's memory
    }
    val isHouseSuggestion = addPostViewModel.postType == UptmConstants.POST_TYPE_HOUSE_SUGGESTION // function remembers the form shape: true = suggest, false = housemate
    val uiState by addPostViewModel.addPostUiState.collectAsStateWithLifecycle()// function collects the UI state from ViewModel
    val scrollState = rememberScrollState() // function creates a scroll state for the form
    var showDatePicker by remember { mutableStateOf(false) } // function remembers if the calendar pop-up is open or closed
    val datePickerState = rememberDatePickerState() // function holds the day the user taps inside the calendar (as millis)
    // function shows a snack bar when post is created successfully
    LaunchedEffect(uiState) { // function watches the state tank
        if (uiState is AddPostUiState.Success) { // function checks the post was created
            addPostViewModel.resetForm() // function empties the backpack BEFORE leaving so next visit is clean
            onPostSuccess() // function navigates back home
        }
    }
    val photoPickerLauncher = rememberLauncherForActivityResult( // function creates an invisible button that opens the phone's photo picker
        contract = ActivityResultContracts.PickMultipleVisualMedia(6) // function allows picking up to 6 pictures in one go
    ) { uris -> addPostViewModel.addPhotos(uris) } // function hands the picked pictures to the ViewModel when the user confirms

    Scaffold( // function creates the basic screen structure with app bar
        topBar = { // function defines the top app bar
            TopAppBar(
                title = { Text("Add Post") }, // function shows the title
                navigationIcon = { // function adds the back button
                    IconButton(onClick = onNavigateBack) { // function handles back click
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // function shows back arrow
                            contentDescription = "Back" // function describes the icon for accessibility
                        )
                    }
                }
            )
        }
    ) { paddingValues -> // function provides padding for the content
        Column( // function stacks the whole form vertically
            modifier = Modifier
                .fillMaxSize() // function fills the screen
                .verticalScroll(scrollState) // function lets the form slide up and down (the scroll engine)
                .padding(paddingValues) // function respects the top app bar
                .padding(horizontal = 16.dp, vertical = 24.dp), // function adds breathing room; the bottom 24.dp stops the last box hiding at the screen edge
            verticalArrangement = Arrangement.spacedBy(12.dp) // function keeps equal gaps between all fields
        ) {

            Text("Photos", style = MaterialTheme.typography.titleMedium) // function shows the photos section header

            Row( // function lines the photo thumbnails up sideways
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), // function lets the row slide sideways when photos overflow
                horizontalArrangement = Arrangement.spacedBy(8.dp) // function leaves gaps between thumbnails
            ) {
                addPostViewModel.selectedPhotoUris.forEach { uri -> // function loops through every picked photo
                    Box { // function stacks the picture and its little x button on top of each other
                        AsyncImage( // function asks Coil to develop and show this picture
                            model = uri, // function hands Coil the photo ticket (Uri)
                            contentDescription = null, // function no talk-back description needed here
                            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)), // function makes a 96dp square with round corners
                            contentScale = ContentScale.Crop // function zooms and crops so the square is fully covered, no white stripes
                        )
                        IconButton( // function makes the tiny remove button
                            onClick = { addPostViewModel.removePhoto(uri) }, // function removes this exact photo when its x is tapped
                            modifier = Modifier.size(24.dp).align(Alignment.TopEnd) // function parks the x in the top-right corner
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove photo", modifier = Modifier.size(16.dp), tint = Color.White) // function draws the small white x
                        }
                    }
                }
                if (addPostViewModel.selectedPhotoUris.size < 6) { // function shows the Add More tile only while we are under 6 photos
                    Box( // function makes the Add More square tile
                        modifier = Modifier
                            .size(96.dp) // function same size as the thumbnails
                            .clip(RoundedCornerShape(12.dp)) // function round corners
                            .border(2.dp, Color.Gray, RoundedCornerShape(12.dp)) // function draws the gray outline (dashed style is Sprint 3 polish)
                            .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, // function fires the photo picker and orders IMAGES ONLY from the menu
                        contentAlignment = Alignment.Center // function centers the plus sign and text
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { // function stacks the plus above the text
                            Icon(Icons.Default.Add, contentDescription = "Add photos") // function draws the plus sign
                            Text("Add More", style = MaterialTheme.typography.labelSmall) // function writes Add More under the plus
                        }
                    }
                }
            }

            Text("You can add up to 6 photos", style = MaterialTheme.typography.bodySmall, color = Color.Gray) // function shows the helper line from the wireframe

            Text("Details", style = MaterialTheme.typography.titleMedium) // function shows a small section header

            OutlinedTextField( // function creates a boxed input with the label sitting on the border
                value = addPostViewModel.title, // function shows the current title text
                onValueChange = { addPostViewModel.title = it }, // function saves every keystroke into the ViewModel
                label = { Text("Post Title") }, // function shows the gray label text
                modifier = Modifier.fillMaxWidth(), // function stretches the box across the screen
                singleLine = true // function keeps the typing on one line
            )

            OutlinedTextField( // function creates the house name box
                value = addPostViewModel.propertyName, // function shows current house name
                onValueChange = { addPostViewModel.propertyName = it }, // function saves house name typing
                label = { Text("House / Property Name") }, // function shows the label
                modifier = Modifier.fillMaxWidth(), // function stretches full width
                singleLine = true // function one line only
            )

            OutlinedTextField( // function creates the location box
                value = addPostViewModel.location, // function shows current location
                onValueChange = { addPostViewModel.location = it }, // function saves location typing
                label = { Text("Location") }, // function shows the label
                modifier = Modifier.fillMaxWidth(), // function stretches full width
                singleLine = true // function one line only
            )

            Row( // function puts two boxes next to each other instead of stacked
                modifier = Modifier.fillMaxWidth(), // function stretches the pair across the screen
                horizontalArrangement = Arrangement.spacedBy(8.dp) // function leaves a small gap between the two boxes
            ) {
                Box(modifier = Modifier.weight(1f)) { // function gives the price box exactly half of the row
                    OutlinedTextField( // function creates the price box
                        value = addPostViewModel.priceText, // function shows current price text
                        onValueChange = { newText -> // function receives typing
                            val onlyDigitsAndDots = newText.all { it.isDigit() || it == '.' } // function checks characters are numbers or dot
                            val atMostOneDot = newText.count { it == '.' } <= 1 // function allows only one dot
                            if (onlyDigitsAndDots && atMostOneDot) { // function gates bad text out
                                addPostViewModel.priceText = newText // function saves clean text
                            }
                        },
                        label = { Text("Price (RM)") }, // function shows the label
                        modifier = Modifier.fillMaxWidth(), // function fills its own half
                        singleLine = true, // function keeps one line
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // function pops up the number pad
                    )
                }
                Box(modifier = Modifier.weight(1f)) { // function gives the bedrooms box the other half
                    UptmDropdown( // function shows the bedrooms dropdown
                        label = "Bedrooms", // function sets the gray label
                        options = UptmConstants.BEDROOM_OPTIONS, // function feeds the fixed bedroom list
                        selected = addPostViewModel.selectedBedrooms, // function shows the current pick
                        onSelect = { choice -> addPostViewModel.selectedBedrooms = choice } // function saves the pick
                    )
                }
            }

            UptmDropdown( // function shows the property type dropdown menu
                label = "Property Type", // function sets the gray label on the box
                options = UptmConstants.PROPERTY_TYPES, // function feeds the menu with Studio/Condominium/Apartment/Landed
                selected = addPostViewModel.selectedPropertyType, // function shows the choice currently picked
                onSelect = { choice -> addPostViewModel.selectedPropertyType = choice } // function saves the tapped choice into the ViewModel
            )

            if (isHouseSuggestion) { // function draws the link box ONLY on the House Suggestion shape
                OutlinedTextField( // function creates the property link box
                    value = addPostViewModel.propertyLink, // function shows the current link text
                    onValueChange = { addPostViewModel.propertyLink = it }, // function saves every keystroke of the link
                    label = { Text("Link to property (PropertyGuru, iProperty, etc.)") }, // function shows the label with examples
                    modifier = Modifier.fillMaxWidth(), // function stretches full width
                    singleLine = true, // function keeps the link on one line
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri) // function pops up the web keyboard with the .com shortcut key
                )
            }
            if (!isHouseSuggestion) { // function draws the gender dropdown ONLY on the Housemate Wanted shape
                // function closes the gender condition
                UptmDropdown( // function shows the gender preference dropdown menu
                    label = "Preferred Housemate Gender", // function sets the gray label on the box
                    options = UptmConstants.GENDER_PREFERENCES, // function feeds the menu with Male/Female
                    selected = addPostViewModel.selectedGender, // function shows the choice currently picked
                    onSelect = { choice ->
                        addPostViewModel.selectedGender = choice
                    } // function saves the tapped choice into the ViewModel
                )
            }
            Text("Facilities", style = MaterialTheme.typography.titleMedium) // function shows the facilities section header

            FlowRow( // function lays chips side by side AND wraps them to the next line when the row is full
                modifier = Modifier.fillMaxWidth(), // function stretches the chip area across the screen
                horizontalArrangement = Arrangement.spacedBy(8.dp), // function leaves gaps between chips sideways
                verticalArrangement = Arrangement.spacedBy(8.dp) // function leaves gaps between chip lines
            ) {
                UptmConstants.FACILITIES.forEach { facility -> // function loops through all 11 facilities from the menu
                    FilterChip( // function creates a tappable on/off chip (the official Material multi-select button)
                        selected = addPostViewModel.selectedFacilities.contains(facility), // function checks if this chip is currently picked (filled color when true)
                        onClick = { addPostViewModel.toggleFacility(facility) }, // function flips the chip on or off using the ViewModel function we wrote earlier
                        label = { Text(facility) } // function writes the facility name on the chip
                    )
                }
            }
            if (!isHouseSuggestion) { // function draws everything inside ONLY on the Housemate Wanted shape
                // function closes the housemate-only section
                Text(
                    "Move-in Date",
                    style = MaterialTheme.typography.titleMedium
                ) // function shows the date section header

                OutlinedButton( // function creates a bordered button that opens the calendar
                    onClick = {
                        showDatePicker = true
                    }, // function opens the calendar pop-up when tapped
                    modifier = Modifier.fillMaxWidth() // function stretches the button across the screen
                ) {
                    Text( // function shows the picked date or the placeholder
                        text = if (addPostViewModel.moveInDateMillis != null) { // function checks if a date was already picked
                            SimpleDateFormat(
                                "d MMM yyyy",
                                LocalLocale.current.platformLocale
                            ).format(Date(addPostViewModel.moveInDateMillis!!)) // function turns the millis number into "16 Sep 2026" for human eyes
                        } else {
                            "Select date" // function shows the placeholder when nothing is picked yet
                        }
                    )
                }

                if (showDatePicker) { // function draws the calendar dialog only while the flag is true
                    DatePickerDialog( // function creates the calendar pop-up with OK and Cancel buttons
                        onDismissRequest = {
                            showDatePicker = false
                        }, // function closes the calendar on Cancel or outside tap
                        confirmButton = { // function defines the OK side
                            TextButton(onClick = { // function handles the OK tap
                                addPostViewModel.moveInDateMillis =
                                    datePickerState.selectedDateMillis // function copies the tapped day into the ViewModel memory
                                showDatePicker = false // function closes the calendar
                            }) { Text("OK") } // function writes OK
                        },
                        dismissButton = { // function defines the Cancel side
                            TextButton(onClick = {
                                showDatePicker = false
                            }) { Text("Cancel") } // function closes without saving anything
                        }
                    ) {
                        DatePicker(state = datePickerState) // function draws the actual month grid inside the dialog
                    }
                }

                Text(
                    "About Us",
                    style = MaterialTheme.typography.titleMedium
                ) // function shows the second section header

                OutlinedTextField( // function creates the big About Us box
                    value = addPostViewModel.description, // function shows current about-us text
                    onValueChange = {
                        addPostViewModel.description = it
                    }, // function saves about-us typing
                    label = { Text("Tell others about yourself, your lifestyle, preferences, etc.") }, // function shows the long label from the wireframe
                    modifier = Modifier.fillMaxWidth()
                        .height(120.dp), // function makes the box tall like the wireframe
                    minLines = 4 // function keeps at least 4 lines visible while typing
                )
            }
            if (uiState is AddPostUiState.Error) { // function shows the red reminder only when something needs fixing
                Text(
                    text = (uiState as AddPostUiState.Error).message, // function pulls the message out of the Error shape
                    color = MaterialTheme.colorScheme.error, // function paints it red
                    style = MaterialTheme.typography.bodySmall // function keeps it small and humble
                )
            }

            Button( // function creates the big Post button from the wireframe
                onClick = { addPostViewModel.createPost() }, // function fires the gather-and-ship when tapped
                modifier = Modifier.fillMaxWidth().height(52.dp), // function stretches it wide and tall like the wireframe
                enabled = uiState !is AddPostUiState.Saving // function freezes the button while saving so double-taps cannot double-post
            ) {
                if (uiState is AddPostUiState.Saving) { // function checks if we are mid-save
                    CircularProgressIndicator( // function shows a tiny spinner inside the button
                        modifier = Modifier.size(20.dp), // function keeps the spinner small
                        color = Color.White, // function paints it white so it shows on the dark button
                        strokeWidth = 2.dp // function makes the ring thin
                    )
                } else {
                    Text("Post", style = MaterialTheme.typography.titleMedium) // function writes the button word
                }
            }
        }
    }
}