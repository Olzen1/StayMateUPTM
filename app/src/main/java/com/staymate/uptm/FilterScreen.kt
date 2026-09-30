package com.staymate.uptm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.SearchViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onApply: () -> Unit
) {
    val draftState by viewModel.draftFilterState.collectAsStateWithLifecycle()

    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    var propertyTypeExpanded by remember { mutableStateOf(false) }
    var furnishedExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = { viewModel.resetDraftFilters() }) {
                Text(
                    text = "Reset",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Price Range (RM)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Price Range (RM)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = draftState.minPrice?.toInt()?.toString() ?: "",
                        onValueChange = { input ->
                            val valDouble = input.toDoubleOrNull()
                            viewModel.updateDraftMinPrice(valDouble)
                        },
                        placeholder = { Text("Min", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )

                    Text(
                        text = "to",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = draftState.maxPrice?.toInt()?.toString() ?: "",
                        onValueChange = { input ->
                            val valDouble = input.toDoubleOrNull()
                            viewModel.updateDraftMaxPrice(valDouble)
                        },
                        placeholder = { Text("Max", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }

            // Bedroom Count
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Bedroom Count",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val bedroomOptions = listOf(1 to "1", 2 to "2", 3 to "3", 4 to "4", 5 to "5+")
                    bedroomOptions.forEach { (count, label) ->
                        val isSelected = draftState.selectedBedrooms.contains(count)
                        Box(
                            modifier = Modifier
                                .width(54.dp)
                                .height(46.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.toggleDraftBedroom(count) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            // Move-in Date Range (Ready from -> Latest date)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Move-in Date Range",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Ready from date
                    val fromText = draftState.moveInFromMs?.let { dateFormat.format(Date(it)) } ?: "Ready from"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                            .clickable { showFromDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = fromText,
                                fontSize = 13.sp,
                                color = if (draftState.moveInFromMs != null) MaterialTheme.colorScheme.onBackground else Color.Gray
                            )
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Pick date",
                                tint = Color.Gray
                            )
                        }
                    }

                    // Ready to date
                    val toText = draftState.moveInToMs?.let { dateFormat.format(Date(it)) } ?: "Latest date"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                            .clickable { showToDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = toText,
                                fontSize = 13.sp,
                                color = if (draftState.moveInToMs != null) MaterialTheme.colorScheme.onBackground else Color.Gray
                            )
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Pick date",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }

            // Property Type Dropdown
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Property Type",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                            .clickable { propertyTypeExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (draftState.propertyType.isNotBlank()) draftState.propertyType else "Select property type",
                                fontSize = 14.sp,
                                color = if (draftState.propertyType.isNotBlank()) MaterialTheme.colorScheme.onBackground else Color.Gray
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = propertyTypeExpanded,
                        onDismissRequest = { propertyTypeExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Any Property Type") },
                            onClick = {
                                viewModel.updateDraftPropertyType("")
                                propertyTypeExpanded = false
                            }
                        )
                        UptmConstants.PROPERTY_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    viewModel.updateDraftPropertyType(type)
                                    propertyTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Furnished Dropdown
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Furnished",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                            .clickable { furnishedExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (draftState.furnishedStatus.isNotBlank()) draftState.furnishedStatus else "Select furnished status",
                                fontSize = 14.sp,
                                color = if (draftState.furnishedStatus.isNotBlank()) MaterialTheme.colorScheme.onBackground else Color.Gray
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = furnishedExpanded,
                        onDismissRequest = { furnishedExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Any Furnished Status") },
                            onClick = {
                                viewModel.updateDraftFurnishedStatus("")
                                furnishedExpanded = false
                            }
                        )
                        UptmConstants.FURNISHED.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    viewModel.updateDraftFurnishedStatus(status)
                                    furnishedExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Gender Preference Dropdown
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Gender Preference",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                            .clickable { genderExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (draftState.genderPreference.isNotBlank()) draftState.genderPreference else "Select gender preference",
                                fontSize = 14.sp,
                                color = if (draftState.genderPreference.isNotBlank()) MaterialTheme.colorScheme.onBackground else Color.Gray
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Any Gender") },
                            onClick = {
                                viewModel.updateDraftGenderPreference("")
                                genderExpanded = false
                            }
                        )
                        UptmConstants.GENDER_PREFERENCES.forEach { gender ->
                            DropdownMenuItem(
                                text = { Text(gender) },
                                onClick = {
                                    viewModel.updateDraftGenderPreference(gender)
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Apply Filter Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    viewModel.applyDraftFilters()
                    onApply()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Apply Filter",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }

    // DatePicker Dialog for Ready From Date
    if (showFromDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = draftState.moveInFromMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        viewModel.updateDraftMoveInDates(ms, draftState.moveInToMs)
                    }
                    showFromDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // DatePicker Dialog for Latest Date
    if (showToDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = draftState.moveInToMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        viewModel.updateDraftMoveInDates(draftState.moveInFromMs, ms)
                    }
                    showToDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
