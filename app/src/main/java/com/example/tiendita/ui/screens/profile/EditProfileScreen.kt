package com.example.tiendita.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.ui.components.AdminButton
import com.example.tiendita.ui.components.AdminCard
import com.example.tiendita.ui.components.AvatarIcon
import com.example.tiendita.ui.components.NexoTextField
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground
import com.example.tiendita.ui.theme.NexoStockTheme
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val photoMsg = stringResource(R.string.msg_photo_future_phase)
    val appliedMsg = stringResource(R.string.msg_changes_applied)

    // Applied state (mock persistency)
    var appliedName by rememberSaveable { mutableStateOf("Administrador NexoStock") }
    var appliedEmail by rememberSaveable { mutableStateOf("administrador@nexostock.local") }
    var appliedPhone by rememberSaveable { mutableStateOf("3312345678") }

    // Form state
    var name by rememberSaveable { mutableStateOf(appliedName) }
    var email by rememberSaveable { mutableStateOf(appliedEmail) }
    var phone by rememberSaveable { mutableStateOf(appliedPhone) }

    // Validation
    val isNameValid = name.trim().length >= 3
    val isEmailValid = email.isNotBlank() && email.matches(Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
    val isPhoneValid = phone.length == 10 && phone.all { it.isDigit() }

    val hasChanges = name != appliedName || email != appliedEmail || phone != appliedPhone
    val canApply = isNameValid && isEmailValid && isPhoneValid && hasChanges

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        ScreenBackground(modifier = modifier.padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                NexoTopBar(title = stringResource(R.string.title_edit_profile), onBackClick = onBack)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.msg_demo_mode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    AdminCard {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AvatarIcon(
                                initials = name.split(" ").let { parts ->
                                    if (parts.size > 1) "${parts[0].first()}${parts[1].first()}"
                                    else parts[0].take(2)
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            AdminButton(
                                text = stringResource(R.string.btn_change_photo),
                                onClick = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(photoMsg)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            NexoTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = stringResource(R.string.label_name),
                                placeholder = "",
                                errorMessage = if (!isNameValid && name.isNotEmpty()) stringResource(R.string.error_name_min_length) else null,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NexoTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = stringResource(R.string.label_email),
                                placeholder = "",
                                errorMessage = if (!isEmailValid && email.isNotEmpty()) stringResource(R.string.error_email_invalid) else null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NexoTextField(
                                value = phone,
                                onValueChange = {
                                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                        phone = it
                                    }
                                },
                                label = stringResource(R.string.label_phone),
                                placeholder = "",
                                errorMessage = if (!isPhoneValid && phone.isNotEmpty()) stringResource(R.string.error_phone_length) else null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done)
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            AdminButton(
                                text = stringResource(R.string.btn_apply_changes),
                                enabled = canApply,
                                onClick = {
                                    appliedName = name
                                    appliedEmail = email
                                    appliedPhone = phone
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(appliedMsg)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditProfileScreenPreview() {
    NexoStockTheme {
        EditProfileScreen(onBack = {})
    }
}
