@file:Suppress("DEPRECATION")

package com.staymate.uptm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.OnboardingUiState
import com.staymate.uptm.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val fullName by viewModel.fullName.collectAsStateWithLifecycle()
    val selectedCourse by viewModel.selectedCourse.collectAsStateWithLifecycle()
    val selectedSemester by viewModel.selectedSemester.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is OnboardingUiState.Success) onComplete()
    }

    val password by viewModel.password.collectAsStateWithLifecycle()
    val confirmPassword by viewModel.confirmPassword.collectAsStateWithLifecycle()
    val accountEmail = viewModel.accountEmail // pre-printed name tag: the Google email, read-only

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // HEADER: the blue gradient welcome banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
        ) {


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Spacer(Modifier.height(14.dp))

                Text(
                    "Welcome to StayMate UPTM 👋",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

            }
        }

        // FORM CARD
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()                              // reserve space for the keyboard
                .verticalScroll(rememberScrollState())     // lets a focused box climb above the keyboard
                .padding(20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // YOUR DETAILS
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "YOUR DETAILS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = viewModel::onFullNameChange,
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = uptmFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    UptmDropdown(
                        "Course",
                        UptmConstants.COURSES,
                        selectedCourse,
                        viewModel::onCourseSelected,
                        leadingIcon = Icons.Default.School   // onboarding keeps its icon; post-form dropdowns pass nothing
                    )
                    UptmDropdown(
                        "Semester",
                        UptmConstants.SEMESTERS,
                        selectedSemester,
                        viewModel::onSemesterSelected,
                        leadingIcon = Icons.Default.School
                    )

                    Spacer(Modifier.height(4.dp))

                    // SECURE YOUR ACCOUNT
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "SECURE YOUR ACCOUNT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = accountEmail, // shows the Google email
                        onValueChange = { }, // empty lambda: typing does nothing
                        enabled = false, // greyed-out look = "official, don't touch"
                        label = { Text("UPTM Email") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color(0xFF2E9E4F)
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = uptmFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { viewModel.onConfirmPasswordChange(it) },
                        label = { Text("Re-enter Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = uptmFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    //  live password strength checklist
                    if (password.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Password strength",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                PasswordRuleRow("At least 6 characters", !password.containsShort())
                                PasswordRuleRow("At least 1 special character", password.hasSpecial())
                                PasswordRuleRow("At least 1 uppercase letter", password.hasUppercase())
                                PasswordRuleRow("At least 1 number", password.hasDigit())
                                if (confirmPassword.isNotEmpty()) {
                                    PasswordRuleRow("Passwords match", password == confirmPassword)
                                }
                            }
                        }
                    }

                    (uiState as? OnboardingUiState.Error)?.let {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                it.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // the big finish button
                    Button(
                        onClick = viewModel::submit,
                        enabled = uiState !is OnboardingUiState.Saving,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (uiState is OnboardingUiState.Saving) {
                            CircularProgressIndicator(
                                Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create My Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

        }
    }
}

// tiny helpers for the password rules
private fun String.containsShort() = length < 6
private fun String.hasSpecial() = any { !it.isLetterOrDigit() }
private fun String.hasUppercase() = any { it.isUpperCase() }
private fun String.hasDigit() = any { it.isDigit() }

// one checklist line: a green ✓ when the rule passes, a red ✕ when it breaks
@Composable
private fun PasswordRuleRow(label: String, passed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (passed) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (passed) Color(0xFF2E9E4F) else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = if (passed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
            fontWeight = if (passed) FontWeight.Normal else FontWeight.SemiBold
        )
    }
}

//// journey chip: ticked = finished, glowing = current, muted = still ahead
//@Composable
//private fun OnboardingStepChip(
//    number: String,
//    label: String,
//    done: Boolean,
//    isCurrent: Boolean = false
//) {
//    Surface(
//        shape = RoundedCornerShape(50),
//        color = when {
//            done -> Color.White
//            isCurrent -> Color.White.copy(alpha = 0.25f)
//            else -> Color.White.copy(alpha = 0.12f)
//        }
//    ) {
//        Row(
//            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            if (done) {
//                Icon(
//                    Icons.Default.Check,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(13.dp)
//                )
//                Spacer(Modifier.width(4.dp))
//            } else {
//                Text(
//                    number,
//                    fontSize = 11.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.7f)
//                )
//                Spacer(Modifier.width(4.dp))
//            }
//            Text(
//                label,
//                fontSize = 11.sp,
//                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
//                color = if (done) MaterialTheme.colorScheme.primary else Color.White
//            )
//        }
//    }
//}
// shared rounded + themed palette for the onboarding text fields
@Composable
private fun uptmFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UptmDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    leadingIcon: ImageVector? = null   // optional: post-form dropdowns call with NO icon, onboarding passes School
) {
    var expanded by remember { mutableStateOf(false) }
    val icon: ImageVector? = leadingIcon
    val iconContent: (@Composable () -> Unit)? = if (icon != null) {
        { Icon(icon, contentDescription = null) }
    } else {
        null
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = iconContent,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(14.dp),
            colors = uptmFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}