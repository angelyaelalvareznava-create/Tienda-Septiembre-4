package com.example.tiendita.ui.screens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiendita.R
import com.example.tiendita.ui.components.AdminButton
import com.example.tiendita.ui.components.AdminCard
import com.example.tiendita.ui.components.NexoTextField
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground
import com.example.tiendita.ui.theme.NexoStockTheme

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val isFormValid = username.isNotBlank() && password.isNotBlank()

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NexoTopBar(title = stringResource(R.string.title_login))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AdminCard {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.title_nexostock),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        NexoTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = stringResource(R.string.label_user),
                            placeholder = stringResource(R.string.placeholder_user),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        NexoTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = stringResource(R.string.label_password),
                            placeholder = stringResource(R.string.placeholder_password),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            visualTransformation = PasswordVisualTransformation()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        AdminButton(
                            text = stringResource(R.string.btn_login),
                            onClick = onLogin,
                            enabled = isFormValid
                        )

                        Text(
                            text = stringResource(R.string.btn_register_link),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(onClick = onRegister)
                                .padding(8.dp)
                        )

                        Text(
                            text = stringResource(R.string.msg_academic_login),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    NexoStockTheme {
        LoginScreen(onLogin = {}, onRegister = {})
    }
}
