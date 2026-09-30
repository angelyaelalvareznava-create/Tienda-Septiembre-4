package com.example.tiendita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.tiendita.ui.navigation.NexoApp
import com.example.tiendita.ui.theme.NexoStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dependencies = (application as NexoApplication).container
        setContent { NexoStockTheme(dynamicColor = false) { Surface { NexoApp(dependencies) } } }
    }
}
