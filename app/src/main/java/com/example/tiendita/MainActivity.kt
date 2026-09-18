package com.example.tiendita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.repository.UserRepository
import com.example.tiendita.ui.components.GameShelfTextField
import com.example.tiendita.ui.theme.GameShelfTheme
import com.example.tiendita.viewmodel.UserFormEvent
import com.example.tiendita.viewmodel.UserViewModel
import com.example.tiendita.viewmodel.UserViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameShelfTheme(dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val database = remember { AppDatabase.getDatabase(context.applicationContext) }
                    val repository = remember { com.example.tiendita.repository.UserRepositoryImpl(database.userDao()) }
                    
                    val viewModel: UserViewModel = viewModel(
                        factory = UserViewModelFactory(repository)
                    )
                    
                    UserFormScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun UserFormScreen(viewModel: UserViewModel) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMsg = stringResource(R.string.msg_success_registration)

    LaunchedEffect(state.registroExitoso) {
        if (state.registroExitoso) {
            snackbarHostState.showSnackbar(
                message = successMsg,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(UserFormEvent.ResetSuccessState)
        }
    }

    LaunchedEffect(state.errorGeneral) {
        state.errorGeneral?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.onEvent(UserFormEvent.ResetErrorState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Encabezado de la aplicación
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.title_gameshelf),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.subtitle_user_registration),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Tarjeta principal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        RowSectionHeader()

                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )

                        Text(
                            text = stringResource(R.string.title_personal_info),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.desc_personal_info),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        GameShelfTextField(
                            value = state.nombre,
                            onValueChange = { viewModel.onEvent(UserFormEvent.OnNombreChanged(it)) },
                            label = stringResource(R.string.label_name),
                            placeholder = stringResource(R.string.placeholder_name),
                            errorMessage = state.errorNombre,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        GameShelfTextField(
                            value = state.apellidos,
                            onValueChange = { viewModel.onEvent(UserFormEvent.OnApellidosChanged(it)) },
                            label = stringResource(R.string.label_last_name),
                            placeholder = stringResource(R.string.placeholder_last_name),
                            errorMessage = state.errorApellidos,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        GameShelfTextField(
                            value = state.direccion,
                            onValueChange = { viewModel.onEvent(UserFormEvent.OnDireccionChanged(it)) },
                            label = stringResource(R.string.label_address),
                            placeholder = stringResource(R.string.placeholder_address),
                            errorMessage = state.errorDireccion,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        GameShelfTextField(
                            value = state.telefono,
                            onValueChange = { viewModel.onEvent(UserFormEvent.OnTelefonoChanged(it)) },
                            label = stringResource(R.string.label_phone),
                            placeholder = stringResource(R.string.placeholder_phone),
                            errorMessage = state.errorTelefono,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            )
                        )

                        Spacer(Modifier.height(2.dp))

                        Button(
                            onClick = { viewModel.onEvent(UserFormEvent.OnSubmit) },
                            enabled = state.isFormValid && !state.guardando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (state.guardando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.width(22.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(stringResource(R.string.btn_saving))
                            } else {
                                Text(stringResource(R.string.btn_save_user), fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = stringResource(R.string.msg_room_storage),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.msg_complete_fields),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun RowSectionHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.height(42.dp).width(42.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "U",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.section_new_registration),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.desc_secure_form),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = stringResource(R.string.badge_room),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
