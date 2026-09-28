package com.example.tiendita.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tiendita.ui.components.AdminButton
import com.example.tiendita.ui.components.AdminCard
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground

@Composable
fun GuestRestrictionScreen(
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit
) {
    ScreenBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            NexoTopBar(title = "Acceso Restringido", onBackClick = onBack)

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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "🔒 Iniciar Sesión Requerido",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Para poder registrar nuevos usuarios o editar tu perfil, debes iniciar sesión con una cuenta de usuario existente. No hay ningún perfil seleccionado en el modo de acceso rápido.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        AdminButton(
                            text = "Iniciar Sesión",
                            onClick = onNavigateToLogin
                        )
                    }
                }
            }
        }
    }
}
