package com.staymate.uptm
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.AddPostUiState
import com.staymate.uptm.viewmodel.AddPostViewModel
import java.text.SimpleDateFormat
import java.util.Date

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

    Scaffold(// function creates the basic screen structure with app bar
        containerColor = colorScheme.background,  // soft blue-white page by day, navy by night (default surface = flat white — too boring)
        topBar = { // function defines the top app bar
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(  // recolors the whole bar in one shot
                    containerColor = colorScheme.primary,  // the bar wears UPTM blue (bright blue at night — automatic)
                    titleContentColor = colorScheme.onPrimary,  // white title by day, navy title at night
                    navigationIconContentColor = colorScheme.onPrimary  // back arrow matches the title
                ),
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
                .verticalScroll(rememberScrollState())  // function lets the form slide up and down (the scroll engine)
                .padding(paddingValues) // function respects the top app bar
                .padding(horizontal = 16.dp, vertical = 12.dp), // function adds breathing room; the bottom 24.dp stops the last box hiding at the screen edge
            verticalArrangement = Arrangement.spacedBy(16.dp) // function keeps equal gaps between all fields
        ) {
            PostTypeBadge(isHouseSuggestion = isHouseSuggestion)  // blue or red pill announcing what the user is building

            //SectionLabel(title = "PHOTOS", counter = "${addPostViewModel.selectedPhotoUris.size}/6")
            //            FormCard {  // the white lunchbox holding all the photo tiles
            //                FlowRow(
            //                    horizontalArrangement = Arrangement.spacedBy(8.dp),  // 8dp gap between tiles, left to right
            //                    verticalArrangement = Arrangement.spacedBy(8.dp)     // 8dp gap when tiles wrap onto a second line
            //                ) {
            //                    addPostViewModel.selectedPhotoUris.forEach { uri ->  // walk every picked photo — no index needed because removePhoto eats a Uri
            //                        PhotoTile(  // one square photo + its red-circle ✕
            //                            imageUri = uri,  // hand the tile its photo ticket
            //                            onRemove = { addPostViewModel.removePhoto(uri) }  // tapping the red circle removes THIS exact photo
            //                        )
            //                    }
            //                    if (addPostViewModel.selectedPhotoUris.size < 6) {  // the dotted empty seat appears only while under the limit
            //                        AddPhotoTile(  // the dashed blue invitation tile
            //                            onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }  // YOUR old launch line, verbatim — fires the picker, IMAGES ONLY
            //                        )
            //                    }
            //                }
            //            }
            //Text("You can add up to 6 photos", style = MaterialTheme.typography.bodySmall, color = Color.Gray) // function shows the helper line from the wireframe

            SectionLabel("DETAILS")  // no counter — nothing to count here

            FormCard {

                OutlinedTextField(
                    value = addPostViewModel.title,  // what the user typed — lives in the ViewModel's memory
                    onValueChange = { addPostViewModel.title = it },  // every keystroke saves straight into the ViewModel
                    label = { RequiredLabel("Post Title") },  // our brick: label + red star (replaces the old plain Text label)
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },  // ✏ pencil — decoration only, no screen-reader words
                    singleLine = true,  // a title never wraps to a second line
                    modifier = Modifier.fillMaxWidth()  // stretch to the card's full inner width
                )

                OutlinedTextField(
                    value = addPostViewModel.propertyName,  // the house / property name
                    onValueChange = { addPostViewModel.propertyName = it },  // keystrokes save into the ViewModel
                    label = { RequiredLabel("House / Property Name") },  // label + red star
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },  // 🏠 little house
                    singleLine = true,  // one line only
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = addPostViewModel.location,  // where the place is
                    onValueChange = { addPostViewModel.location = it },  // keystrokes save into the ViewModel
                    label = { RequiredLabel("Location") },  // label + red star
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },  // 📍 map pin
                    singleLine = true,  // one line only
                    modifier = Modifier.fillMaxWidth()
                )

                Row( // function puts two boxes next to each other instead of stacked
                    modifier = Modifier.fillMaxWidth(), // function stretches the pair across the screen
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // function leaves a small gap between the two boxes
                ) {
                    Box(modifier = Modifier.weight(1f)) { // function gives the price box exactly half of the row
                        OutlinedTextField( // function creates the price box
                            value = addPostViewModel.priceText, // function shows current price text
                            onValueChange = { newText -> // function receives typing
                                val onlyDigitsAndDots =
                                    newText.all { it.isDigit() || it == '.' } // function checks characters are numbers or dot
                                val atMostOneDot =
                                    newText.count { it == '.' } <= 1 // function allows only one dot
                                if (onlyDigitsAndDots && atMostOneDot) { // function gates bad text out
                                    addPostViewModel.priceText =
                                        newText // function saves clean text
                                }
                            },
                            label = { RequiredLabel("Price") },  // star added; "(RM)" leaves the label because RM now lives INSIDE the field
                            modifier = Modifier.fillMaxWidth(), // function fills its own half
                            singleLine = true, // function keeps one line
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // function pops up the number pad
                            prefix = { Text("RM", color = colorScheme.onSurfaceVariant) }, // "RM" parked at the field's left edge — quiet gray-blue, like the RM printed on a price tag
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        // function gives the bedrooms box the other half
                        UptmDropdown( // function shows the bedrooms dropdown
                            label = "Bedrooms", // function sets the gray label
                            options = UptmConstants.BEDROOM_OPTIONS, // function feeds the fixed bedroom list
                            selected = addPostViewModel.selectedBedrooms, // function shows the current pick
                            onSelect = { choice ->
                                addPostViewModel.selectedBedrooms = choice
                            } // function saves the pick
                        )
                    }
                }
                Box() {
                    UptmDropdown( // function shows the property type dropdown menu
                        label = "Property Type", // function sets the gray label on the box
                        options = UptmConstants.PROPERTY_TYPES, // function feeds the menu with Studio/Condominium/Apartment/Landed
                        selected = addPostViewModel.selectedPropertyType, // function shows the choice currently picked
                        onSelect = { choice ->
                            addPostViewModel.selectedPropertyType = choice
                        } // function saves the tapped choice into the ViewModel
                    )
                }
                if (isHouseSuggestion) { // function draws the link box ONLY on the House Suggestion shape
                    OutlinedTextField( // function creates the property link box
                        value = addPostViewModel.propertyLink, // function shows the current link text
                        onValueChange = {
                            addPostViewModel.propertyLink = it
                        }, // function saves every keystroke of the link
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
            }
            SectionLabel("FACILITIES", counter = "${addPostViewModel.selectedFacilities.size} picked")  // e.g. 4 picked
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
                    minLines = 4,// function keeps at least 4 lines visible while typing
                )
            }
            if (uiState is AddPostUiState.Error) { // function shows the red reminder only when something needs fixing
                Text(
                    text = (uiState as AddPostUiState.Error).message, // function pulls the message out of the Error shape
                    color = colorScheme.error, // function paints it red
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

@Composable
fun PostTypeBadge(isHouseSuggestion: Boolean) {
    val container = if (isHouseSuggestion) colorScheme.primary else colorScheme.secondary  // blue pill or red pill
    val labelColor = if (isHouseSuggestion) colorScheme.onPrimary else colorScheme.onSecondary  // text color that matches each pill
    Surface(color = container, contentColor = labelColor, shape = RoundedCornerShape(percent = 50)) {  // percent 50 = fully rounded ends = capsule
        Text(
            text = if (isHouseSuggestion) "HOUSE SUGGESTION" else "HOUSEMATE WANTED",  // same words as the type dialog — one dialect rule
            style = MaterialTheme.typography.labelMedium,  // small "stamp" text size
            letterSpacing = 1.sp,  // extra space between letters = official stamp look
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)  // breathing room inside the pill
        )
    }
}

@Composable
fun SectionLabel(title: String, counter: String? = null) {  // counter optional: null = don't show one
    Row(verticalAlignment = Alignment.CenterVertically) {  // title left, counter right
        Text(
            text = title,  // e.g. "PHOTOS"
            style = MaterialTheme.typography.labelLarge,  // one size up from the badge text
            color = colorScheme.primary,  // UPTM blue signage
            fontWeight = FontWeight.SemiBold,  // slightly bolder so it leads its section
            letterSpacing = 1.sp,  // same stamp spacing as the badge
            modifier = Modifier.weight(1f)  // eats all spare width → pushes counter to the far right edge
        )
        if (counter != null) {  // only draws a counter when we passed one
            Text(
                text = counter,  // e.g. "3/6"
                style = MaterialTheme.typography.labelLarge,  // same size as the title
                color = colorScheme.onSurfaceVariant  // quiet grey-blue — info, not shouting
            )
        }
    }
}

@Composable
fun FormCard(content: @Composable ColumnScope.() -> Unit) {  // content slot: an empty labeled space we fill later
    Surface(
        color = colorScheme.surface,  // white card by day, navy card by night — theme decides
        contentColor = colorScheme.onSurface,  // anything inside defaults to ink/night text
        shape = RoundedCornerShape(20.dp),  // the soft corners from the blueprint
        tonalElevation = 2.dp,  // gentle lift; at night it adds a blue glow tint
        shadowElevation = 2.dp,  // the soft drop shadow that makes the card "float"
        modifier = Modifier.fillMaxWidth()  // cards always stretch the full page width
    ) {
        Column(
            modifier = Modifier.padding(16.dp),  // inner padding so content never touches card edges
            verticalArrangement = Arrangement.spacedBy(12.dp),  // NEW: 12dp air between everything inside the card
            content = content  // whatever we type between the card's braces lands here
        )
    }
}
@Composable
fun PhotoTile(imageUri: Uri, onRemove: () -> Unit) {  // one square photo + its red-circle ✕
    Box(modifier = Modifier.size(100.dp)) {  // fixed square stage: photo fills it, ✕ parks on top
        AsyncImage(
            model = imageUri,  // the photo (a local Uri — same kind your current thumbs use)
            contentDescription = "Selected photo",  // words a blind user's screen reader says aloud
            contentScale = ContentScale.Crop,  // crop = fill the square like a passport photo, no stretching
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))  // rounded photo corners, matching the card's soft look
        )
        Box(  // the small red circle holding the ✕
            contentAlignment = Alignment.Center,  // keep the ✕ perfectly centered inside its circle
            modifier = Modifier
                .align(Alignment.TopEnd)  // park in the photo's top-right corner
                .padding(4.dp)  // tiny gap so the circle doesn't kiss the tile edge
                .size(28.dp)  // circle size
                .clip(CircleShape)  // cut the shape into a circle
                .background(colorScheme.secondary)  // brand-red circle — red budget: delete is a red job
                .clickable { onRemove() }  // tapping the circle removes this photo
        ) {
            Icon(
                imageVector = Icons.Default.Close,  // the ✕ symbol (same icon your old code used)
                contentDescription = "Remove photo",  // screen-reader words
                tint = colorScheme.onSecondary,  // white ✕ — the color that matches red circles
                modifier = Modifier.size(18.dp)  // icon size inside the 28dp circle
            )
        }
    }
}
@Composable
fun AddPhotoTile(onClick: () -> Unit) {  // empty photo slot: dotted frame + ＋ + "Add"
    val frameColor = colorScheme.primary  // grab the blue BEFORE painting (the brush can't ask the theme itself)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,  // ＋ and "Add" stack centered
        verticalArrangement = Arrangement.Center,  // the stack floats to the middle of the square
        modifier = Modifier
            .size(100.dp)  // same size as a photo tile → rows look even
            .clip(RoundedCornerShape(14.dp))  // same corners as photo tiles
            .drawBehind {  // paint the dotted frame ourselves — Compose has NO ready-made dotted border
                drawRoundRect(  // draw a rounded rectangle outline...
                    color = frameColor,  // ...in UPTM blue...
                    cornerRadius = CornerRadius(14.dp.toPx()),  // ...with matching corner rounding...
                    style = Stroke(  // ...as an OUTLINE (not a fill)...
                        width = 2.dp.toPx(),  // ...nice and thin...
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(
                                12f,
                                10f
                            )
                        )  // ...dotted: 12px dash, 10px gap, repeat forever
                    )
                )
            }
            .clickable { onClick() }  // tapping the empty seat opens the phone gallery
    ) {
        Icon(
            imageVector = Icons.Default.Add,  // the ＋ symbol
            contentDescription = null,  // decorative — the "Add" word below already tells the story
            tint = frameColor,  // blue ＋ matching the frame
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))  // small breathing gap between ＋ and the word
        Text(
            text = "Add",  // the invitation to tap
            style = MaterialTheme.typography.labelMedium,  // small, quiet text
            color = frameColor
        )
    }
}
@Composable
fun RequiredLabel(label: String) {  // draws "Post Title" + a red * = "you must fill this in"
    Row(verticalAlignment = Alignment.Top) {  // asterisk hugs the text's top edge, like a little flag
        Text(text = label)  // the label word — NO style set, so the field dresses it in its own label outfit
        Text(
            text = " *",  // the required star (leading space keeps it off the word)
            color = colorScheme.secondary  // brand red — red budget rule: required markers are red jobs
        )
    }
}