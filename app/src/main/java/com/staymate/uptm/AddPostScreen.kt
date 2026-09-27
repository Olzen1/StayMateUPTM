package com.staymate.uptm

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.AddPostUiState
import com.staymate.uptm.viewmodel.AddPostViewModel
import java.text.SimpleDateFormat
import java.util.Date

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
    val uiState by addPostViewModel.addPostUiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(uiState) {
        if (uiState is AddPostUiState.Success) {
            addPostViewModel.resetForm()
            onPostSuccess()
        }
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
                title = { Text("Add Post") },
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

            PostTypeBadge(isHouseSuggestion = isHouseSuggestion)

            if (addPostViewModel.currentStep == 1) {
                // STEP 1: DETAILS
                SectionLabel("DETAILS")

                FormCard {
                    OutlinedTextField(
                        value = addPostViewModel.title,
                        onValueChange = { addPostViewModel.title = it },
                        label = { RequiredLabel("Post Title") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addPostViewModel.propertyName,
                        onValueChange = { addPostViewModel.propertyName = it },
                        label = { RequiredLabel("House / Property Name") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addPostViewModel.location,
                        onValueChange = { addPostViewModel.location = it },
                        label = { RequiredLabel("Location") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

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
                                label = { RequiredLabel("Price") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                prefix = { Text("RM", color = colorScheme.onSurfaceVariant) }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            UptmDropdown(
                                label = "Bedrooms",
                                options = UptmConstants.BEDROOM_OPTIONS,
                                selected = addPostViewModel.selectedBedrooms,
                                onSelect = { choice -> addPostViewModel.selectedBedrooms = choice }
                            )
                        }
                    }

                    if (!isHouseSuggestion) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = addPostViewModel.depositText,
                                    onValueChange = { addPostViewModel.depositText = it },
                                    label = { Text("Deposit (RM)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    prefix = { Text("RM", color = colorScheme.onSurfaceVariant) }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = addPostViewModel.currentHousematesText,
                                    onValueChange = { addPostViewModel.currentHousematesText = it },
                                    label = { Text("Current Roommates") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }

                        OutlinedTextField(
                            value = addPostViewModel.rentPerPersonText,
                            onValueChange = { addPostViewModel.rentPerPersonText = it },
                            label = { Text("Rent Per Person (RM)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            prefix = { Text("RM", color = colorScheme.onSurfaceVariant) }
                        )
                    }

                    UptmDropdown(
                        label = "Furnished Status",
                        options = UptmConstants.FURNISHED,
                        selected = addPostViewModel.selectedFurnished,
                        onSelect = { addPostViewModel.selectedFurnished = it }
                    )

                    UptmDropdown(
                        label = "Property Type",
                        options = UptmConstants.PROPERTY_TYPES,
                        selected = addPostViewModel.selectedPropertyType,
                        onSelect = { choice -> addPostViewModel.selectedPropertyType = choice }
                    )

                    if (isHouseSuggestion) {
                        OutlinedTextField(
                            value = addPostViewModel.propertyLink,
                            onValueChange = { addPostViewModel.propertyLink = it },
                            label = { Text("Link to property (PropertyGuru, iProperty, etc.)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                        )
                    }

                    if (!isHouseSuggestion) {
                        UptmDropdown(
                            label = "Preferred Housemate Gender",
                            options = UptmConstants.GENDER_PREFERENCES,
                            selected = addPostViewModel.selectedGender,
                            onSelect = { choice -> addPostViewModel.selectedGender = choice }
                        )
                    }
                }

                SectionLabel("FACILITIES", counter = "${addPostViewModel.selectedFacilities.size} picked")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UptmConstants.FACILITIES.forEach { facility ->
                        FilterChip(
                            selected = addPostViewModel.selectedFacilities.contains(facility),
                            onClick = { addPostViewModel.toggleFacility(facility) },
                            label = { Text(facility) }
                        )
                    }
                }

                if (!isHouseSuggestion) {
                    Text(
                        "Move-in Date",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (addPostViewModel.moveInDateMillis != null) {
                                SimpleDateFormat(
                                    "d MMM yyyy",
                                    LocalLocale.current.platformLocale
                                ).format(Date(addPostViewModel.moveInDateMillis!!))
                            } else {
                                "Select date"
                            }
                        )
                    }

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    addPostViewModel.moveInDateMillis = datePickerState.selectedDateMillis
                                    showDatePicker = false
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    Text(
                        "About Us",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = addPostViewModel.description,
                        onValueChange = { addPostViewModel.description = it },
                        label = { Text("Tell others about yourself, your lifestyle, preferences, etc.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        minLines = 4
                    )
                }

                if (uiState is AddPostUiState.Error) {
                    Text(
                        text = (uiState as AddPostUiState.Error).message,
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = { addPostViewModel.goToNextStep() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Next: Contact Information", style = MaterialTheme.typography.titleMedium)
                }

            } else {
                // STEP 2: CONTACT INFORMATION
                SectionLabel("CONTACT INFORMATION")

                FormCard {
                    OutlinedTextField(
                        value = addPostViewModel.contactPhone,
                        onValueChange = { addPostViewModel.contactPhone = it },
                        label = { RequiredLabel("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addPostViewModel.contactEmail,
                        onValueChange = { addPostViewModel.contactEmail = it },
                        label = { Text("Email Address (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
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
                        minLines = 3
                    )
                }

                if (uiState is AddPostUiState.Error) {
                    Text(
                        text = (uiState as AddPostUiState.Error).message,
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { addPostViewModel.goToPreviousStep() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Back", style = MaterialTheme.typography.titleMedium)
                    }

                    Button(
                        onClick = { addPostViewModel.createPost() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        enabled = uiState !is AddPostUiState.Saving
                    ) {
                        if (uiState is AddPostUiState.Saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Post", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
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
        // Step 1
        StepItem(
            stepNumber = 1,
            label = "Details",
            isActive = currentStep == 1,
            onClick = { onStepClick(1) }
        )

        Spacer(modifier = Modifier.width(20.dp))

        // Step 2
        StepItem(
            stepNumber = 2,
            label = "Contact Information",
            isActive = currentStep == 2,
            onClick = { onStepClick(2) }
        )
    }
}

@Composable
private fun StepItem(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
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
                    color = if (isActive) MaterialTheme.colorScheme.primary else Color(0xFFC4CBD4),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) MaterialTheme.colorScheme.primary else Color(0xFF8C96A3)
        )
    }
}

@Composable
fun PostTypeBadge(isHouseSuggestion: Boolean) {
    val container = if (isHouseSuggestion) colorScheme.primary else colorScheme.secondary
    val labelColor = if (isHouseSuggestion) colorScheme.onPrimary else colorScheme.onSecondary
    Surface(color = container, contentColor = labelColor, shape = RoundedCornerShape(percent = 50)) {
        Text(
            text = if (isHouseSuggestion) "HOUSE SUGGESTION" else "HOUSEMATE WANTED",
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SectionLabel(title: String, counter: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
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
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun RequiredLabel(label: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = label)
        Text(
            text = " *",
            color = colorScheme.secondary
        )
    }
}
