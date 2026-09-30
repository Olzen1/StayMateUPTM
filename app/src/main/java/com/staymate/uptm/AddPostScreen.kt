package com.staymate.uptm

// base-android tools to read the slip and write a real file
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.AddPostUiState
import com.staymate.uptm.viewmodel.AddPostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPostScreen(
    addPostViewModel: AddPostViewModel,
    postType: String,
    onNavigateBack: () -> Unit,
    onPostSuccess: () -> Unit
) {
    LaunchedEffect(postType) {
        addPostViewModel.postType = postType
    }
    val isHouseSuggestion = addPostViewModel.postType == UptmConstants.POST_TYPE_HOUSE_SUGGESTION
    val isFindingGroup = addPostViewModel.postType == UptmConstants.POST_TYPE_GROUP_FINDING
    val uiState by addPostViewModel.addPostUiState.collectAsStateWithLifecycle()
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    LaunchedEffect(uiState) {
        if (uiState is AddPostUiState.Success) {
            addPostViewModel.resetForm()
            onPostSuccess()
        }
    }

    val topBarTitle = when {
        isFindingGroup -> "Find a Group"
        addPostViewModel.editingPostId != null -> "Edit Post"
        else ->
            if (isHouseSuggestion) "House Suggestion" else "Housemate Wanted"
    }
    val topBarSubtitle = when {
        isFindingGroup -> "Create a group finding post"
        addPostViewModel.editingPostId != null -> "Update your post details"
        else -> "Create a new post"
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary,
                    navigationIconContentColor = colorScheme.onPrimary
                ),
                title = {
                    Column {
                        Text(topBarTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            topBarSubtitle,
                            fontSize = 11.sp,
                            color = colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (addPostViewModel.currentStep == 2) {
                            addPostViewModel.goToPreviousStep()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stepper Header: 1. Details  2. Contact Information
            StepProgressHeader(
                currentStep = addPostViewModel.currentStep,
                onStepClick = { step ->
                    if (step == 1) {
                        addPostViewModel.goToPreviousStep()
                    } else if (step == 2) {
                        addPostViewModel.goToNextStep()
                    }
                }
            )
            if (addPostViewModel.currentStep == 1) {
                // STEP 1: DETAILS
                if (isFindingGroup) {
                    // FINDING A GROUP STEP 1 FIELDS
                    SectionLabel("GROUP DETAILS", icon = Icons.Default.Group)

                    FormCard {
                        OutlinedTextField(
                            value = addPostViewModel.title,
                            onValueChange = { addPostViewModel.title = it },
                            label = { Text("Title") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = postFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        UptmDropdown(
                            label = "Gender *",
                            options = UptmConstants.GENDER_PREFERENCES,
                            selected = addPostViewModel.selectedGender,
                            onSelect = { addPostViewModel.selectedGender = it }
                        )

                        // Price Range (RM)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Price Range (RM)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = addPostViewModel.priceText,
                                    onValueChange = { addPostViewModel.priceText = it },
                                    placeholder = { Text("Min", color = Color.Gray, fontSize = 14.sp) },
                                    prefix = { Text("RM ", color = colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = postFieldColors()
                                )

                                Text(
                                    text = "to",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )

                                OutlinedTextField(
                                    value = addPostViewModel.maxPriceText,
                                    onValueChange = { addPostViewModel.maxPriceText = it },
                                    placeholder = { Text("Max", color = Color.Gray, fontSize = 14.sp) },
                                    prefix = { Text("RM ", color = colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = postFieldColors()
                                )
                            }
                        }
                    }

                    SectionLabel("MOVE-IN & PROPERTY", icon = Icons.Default.CalendarMonth)

                    FormCard {
                        // Move-in Date Range
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Move-in Date",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val fromText = addPostViewModel.moveInDateMillis?.let { dateFormat.format(Date(it)) } ?: "Move In Date (Optional)"
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .border(
                                            1.dp,
                                            if (addPostViewModel.moveInDateMillis != null) colorScheme.primary.copy(alpha = 0.5f) else colorScheme.outlineVariant,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { showFromDatePicker = true },
                                    shape = RoundedCornerShape(14.dp),
                                    color = colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = fromText,
                                            fontSize = 13.sp,
                                            color = if (addPostViewModel.moveInDateMillis != null) colorScheme.onSurface else Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = "Pick date",
                                            tint = colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        UptmDropdown(
                            label = "Property Type (Optional)",
                            options = UptmConstants.PROPERTY_TYPES,
                            selected = addPostViewModel.selectedPropertyType,
                            onSelect = { choice -> addPostViewModel.selectedPropertyType = choice }
                        )

                        UptmDropdown(
                            label = "Furnished (Optional)",
                            options = UptmConstants.FURNISHED,
                            selected = addPostViewModel.selectedFurnished,
                            onSelect = { choice -> addPostViewModel.selectedFurnished = choice }
                        )

                        OutlinedTextField(
                            value = addPostViewModel.description,
                            onValueChange = { addPostViewModel.description = it },
                            label = { Text("Other Information (Optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            minLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            colors = postFieldColors()
                        )
                    }
                } else {
                    // HOUSE SUGGESTION / HOUSEMATE WANTED FIELDS
                    SectionLabel("POST DETAILS", icon = Icons.Default.Home)

                    FormCard {
                        OutlinedTextField(
                            value = addPostViewModel.title,
                            onValueChange = { addPostViewModel.title = it },
                            label = { Text("Post Title *") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = postFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = addPostViewModel.propertyName,
                            onValueChange = { addPostViewModel.propertyName = it },
                            label = { Text("House / Property Name *") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = postFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = addPostViewModel.location,
                            onValueChange = { addPostViewModel.location = it },
                            label = { Text("Location *") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = postFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SectionLabel("MONEY & ROOMS", icon = Icons.Default.Info)

                    FormCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = addPostViewModel.priceText,
                                    onValueChange = { newText ->
                                        val onlyDigitsAndDots = newText.all { it.isDigit() || it == '.' }
                                        val atMostOneDot = newText.count { it == '.' } <= 1
                                        if (onlyDigitsAndDots && atMostOneDot) {
                                            addPostViewModel.priceText = newText
                                        }
                                    },
                                    label = { Text("Price *") },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {Text("per month")},
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = postFieldColors(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    prefix = { Text("RM ", color = colorScheme.onSurfaceVariant) }
                                )
                            }
                            if (!isHouseSuggestion)
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = addPostViewModel.depositText,
                                    onValueChange = { addPostViewModel.depositText = it },
                                    label = { Text("Deposit (RM)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = postFieldColors(),
                                    prefix = { Text("RM ", color = colorScheme.onSurfaceVariant) }
                                )
                            }
                        }

                        if (!isHouseSuggestion) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                    OutlinedTextField(
                                        value = addPostViewModel.currentHousematesText,
                                        onValueChange = {
                                            addPostViewModel.currentHousematesText = it
                                        },
                                        label = { Text("Current Roommates") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = postFieldColors()
                                    )


                            }
                        }

                        UptmDropdown(
                            label = "Bedrooms *",
                            options = UptmConstants.BEDROOM_OPTIONS,
                            selected = addPostViewModel.selectedBedrooms,
                            onSelect = { choice -> addPostViewModel.selectedBedrooms = choice }
                        )

                        UptmDropdown(
                            label = "Furnished Status *",
                            options = UptmConstants.FURNISHED,
                            selected = addPostViewModel.selectedFurnished,
                            onSelect = { addPostViewModel.selectedFurnished = it }
                        )

                        UptmDropdown(
                            label = "Property Type *",
                            options = UptmConstants.PROPERTY_TYPES,
                            selected = addPostViewModel.selectedPropertyType,
                            onSelect = { choice ->
                                addPostViewModel.selectedPropertyType = choice
                            }
                        )

                        if (!isHouseSuggestion) {
                            UptmDropdown(
                                label = "Preferred Housemate Gender *",
                                options = UptmConstants.GENDER_PREFERENCES,
                                selected = addPostViewModel.selectedGender,
                                onSelect = { choice -> addPostViewModel.selectedGender = choice }
                            )
                        }

                        if (isHouseSuggestion) {
                            OutlinedTextField(
                                value = addPostViewModel.propertyLink,
                                onValueChange = { addPostViewModel.propertyLink = it },
                                label = { Text("Link to property (PropertyGuru, iProperty, etc.)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = postFieldColors(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                            )
                        }
                    }
                    if (!isHouseSuggestion) {
                        SectionLabel(
                            "MOVE IN DATE",

                            icon = Icons.Default.Check
                        )
                        FormCard {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            )
                            {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val fromText = addPostViewModel.moveInDateMillis?.let {
                                        dateFormat.format(Date(it))
                                    } ?: "Move In Date (Optional)"
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .border(
                                                1.dp,
                                                if (addPostViewModel.moveInDateMillis != null) colorScheme.primary.copy(
                                                    alpha = 0.5f
                                                ) else colorScheme.outlineVariant,
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { showFromDatePicker = true },
                                        shape = RoundedCornerShape(14.dp),
                                        color = colorScheme.surface
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = fromText,
                                                fontSize = 13.sp,
                                                color = if (addPostViewModel.moveInDateMillis != null) colorScheme.onSurface else Color.Gray,
                                                textAlign = TextAlign.Center
                                            )
                                            Icon(
                                                Icons.Default.CalendarMonth,
                                                contentDescription = "Pick date",
                                                tint = colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    SectionLabel(
                        "FACILITIES",
                        counter = "${addPostViewModel.selectedFacilities.size} picked",
                        icon = Icons.Default.Check
                    )
                    FormCard {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UptmConstants.FACILITIES.forEach { facility ->
                                val isSelected = addPostViewModel.selectedFacilities.contains(facility)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { addPostViewModel.toggleFacility(facility) },
                                    label = { Text(facility) }
                                )
                            }
                        }
                    }

                    if (!isHouseSuggestion) {
                        SectionLabel("ABOUT US", icon = Icons.Default.Info)
                        FormCard {
                            OutlinedTextField(
                                value = addPostViewModel.description,
                                onValueChange = { addPostViewModel.description = it },
                                label = { Text("Tell others about yourself, your lifestyle, preferences, etc.") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                minLines = 4,
                                shape = RoundedCornerShape(14.dp),
                                colors = postFieldColors()
                            )
                        }
                    }
                }

                if (uiState is AddPostUiState.Error) {
                    ErrorBanner(message = (uiState as AddPostUiState.Error).message)
                }

                Button(
                    onClick = { addPostViewModel.goToNextStep() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Text("Next: Contact Information", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

            } else {
                // STEP 2: CONTACT INFORMATION
                SectionLabel("CONTACT INFORMATION", icon = Icons.Default.Phone)

                FormCard {
                    OutlinedTextField(
                        value = addPostViewModel.contactPhone,
                        onValueChange = { addPostViewModel.contactPhone = it },
                        label = { Text("Phone Number *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = postFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addPostViewModel.contactEmail,
                        onValueChange = { addPostViewModel.contactEmail = it },
                        label = { Text("Email Address (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = postFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    UptmDropdown(
                        label = "Gender *",
                        options = listOf("Male", "Female"),
                        selected = addPostViewModel.contactGender,
                        onSelect = { addPostViewModel.contactGender = it }
                    )

                    OutlinedTextField(
                        value = addPostViewModel.contactWhatsapp,
                        onValueChange = { addPostViewModel.contactWhatsapp = it },
                        label = { Text("WhatsApp Number (Optional)") },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = postFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addPostViewModel.contactOtherInfo,
                        onValueChange = { addPostViewModel.contactOtherInfo = it },
                        label = { Text("Other Information (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        minLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        colors = postFieldColors()
                    )
                }

                if (uiState is AddPostUiState.Error) {
                    ErrorBanner(message = (uiState as AddPostUiState.Error).message)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { addPostViewModel.goToPreviousStep() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Back", style = MaterialTheme.typography.titleMedium)
                    }

                    Button(
                        onClick = { addPostViewModel.createPost() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        enabled = uiState !is AddPostUiState.Saving,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        if (uiState is AddPostUiState.Saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (addPostViewModel.editingPostId != null) "Edit Post" else "Post",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // DatePicker dialogs for Finding a Group date range
    if (showFromDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = addPostViewModel.moveInDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    addPostViewModel.moveInDateMillis = datePickerState.selectedDateMillis
                    showFromDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showToDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = addPostViewModel.moveInDateToMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    addPostViewModel.moveInDateToMillis = datePickerState.selectedDateMillis
                    showToDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// error chip: red-tinted card so validation slip-ups read clearly at a glance
@Composable
private fun ErrorBanner(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            color = colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun StepProgressHeader(
    currentStep: Int,
    onStepClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Step 1: active on step 1, done (✓) once we reach step 2
        StepItem(
            stepNumber = 1,
            label = "Details",
            isActive = currentStep == 1,
            isDone = currentStep > 1,
            onClick = { onStepClick(1) }
        )

        // progress connector: lights up primary once step 2 is reached
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (currentStep >= 2) colorScheme.primary
                    else colorScheme.surfaceVariant
                )
        )

        // Step 2
        StepItem(
            stepNumber = 2,
            label = "Contact",
            isActive = currentStep == 2,
            isDone = false,
            onClick = { onStepClick(2) }
        )
    }
}

@Composable
private fun StepItem(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isDone: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = when {
                        isDone -> colorScheme.primary
                        isActive -> colorScheme.primary
                        else -> colorScheme.surfaceVariant
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(15.dp)
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    color = if (isActive) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = when {
                isActive -> FontWeight.Bold
                isDone -> FontWeight.SemiBold
                else -> FontWeight.Medium
            },
            color = when {
                isActive -> colorScheme.primary
                isDone -> colorScheme.primary
                else -> Color(0xFF8C96A3)
            }
        )
    }
}

@Composable
fun SectionLabel(title: String, counter: String? = null, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // little accent bar before the label — echoes the onboarding section headers
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.weight(1f)
        )
        if (counter != null) {
            Text(
                text = counter,
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = colorScheme.surface,
        contentColor = colorScheme.onSurface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

// shared rounded + themed palette for every post-form text field
@Composable
private fun postFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = colorScheme.primary,
    unfocusedBorderColor = colorScheme.outlineVariant,
    focusedContainerColor = colorScheme.surface,
    unfocusedContainerColor = colorScheme.surface
)