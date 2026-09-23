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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiendita.ui.components.AdminCard
import com.example.tiendita.ui.components.NexoTopBar
import com.example.tiendita.ui.components.ScreenBackground
import com.example.tiendita.ui.theme.NexoStockTheme

@Composable
fun FeaturePlaceholderScreen(
    title: String,
    description: String,
    onBack: () -> Unit
) {
    ScreenBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            NexoTopBar(title = title, onBackClick = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AdminCard {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "En construcción",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FeaturePlaceholderScreenPreview() {
    NexoStockTheme {
        FeaturePlaceholderScreen(
            title = "Módulo X",
            description = "Esta funcionalidad estará disponible en la próxima versión.",
            onBack = {}
        )
    }
}
