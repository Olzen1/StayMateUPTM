package com.staymate.uptm
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.OnboardingUiState
import com.staymate.uptm.viewmodel.OnboardingViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation


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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to StayMate UPTM", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text("Tell us who you are", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = viewModel::onFullNameChange,
            label = { Text("Full Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        UptmDropdown("Course", UptmConstants.COURSES, selectedCourse, viewModel::onCourseSelected)
        Spacer(Modifier.height(12.dp))
        UptmDropdown("Semester", UptmConstants.SEMESTERS, selectedSemester, viewModel::onSemesterSelected)
        Spacer(Modifier.height(8.dp))

        (uiState as? OnboardingUiState.Error)?.let {
            Text(it.message, color = Color(0xFFFF1744), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }
        val accountEmail = viewModel.accountEmail // pre-printed name tag: the Google email, read-only
        val password by viewModel.password.collectAsStateWithLifecycle() // live password text from the VM
        val confirmPassword by viewModel.confirmPassword.collectAsStateWithLifecycle() // live re-typed text from the VM
        val passwordErrors = viewModel.getPasswordErrors() // bouncer checklist; recomputes on every recomposition

        OutlinedTextField(
            value = accountEmail, // shows the Google email
            onValueChange = { }, // empty lambda: new text is thrown away, so typing does nothing
            enabled = false, // greyed-out look = "official, don't touch"
            label = { Text("UPTM Email") }, // tells them which email the password glues to
            modifier = Modifier.fillMaxWidth() // match the other fields
        )

        OutlinedTextField(
            value = password, // current password text
            onValueChange = { viewModel.onPasswordChange(it) }, // every keystroke goes to the VM
            label = { Text("Password") }, // field label
            visualTransformation = PasswordVisualTransformation(), // shows dots instead of real characters
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // soft keyboard switches to password mode
            modifier = Modifier.fillMaxWidth() // match the other fields
        )

        OutlinedTextField(
            value = confirmPassword, // current re-typed text
            onValueChange = { viewModel.onConfirmPasswordChange(it) }, // every keystroke goes to the VM
            label = { Text("Re-enter Password") }, // field label
            visualTransformation = PasswordVisualTransformation(), // dots again
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // password keyboard again
            modifier = Modifier.fillMaxWidth() // match the other fields
        )

        if (password.isNotEmpty()) { // only show the checklist once they start typing, so an empty form stays quiet
            passwordErrors.forEach { errorText -> // loop over every broken rule
                Text(
                    text = "• $errorText", // bullet + the rule text
                    color = MaterialTheme.colorScheme.error, // red = "fix me"
                    style = MaterialTheme.typography.bodySmall // small helper text
                )
            }
        }
        Button(
            onClick = viewModel::submit,
            enabled = uiState !is OnboardingUiState.Saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is OnboardingUiState.Saving) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
            } else {
                Text("Continue")
            }

        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UptmDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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