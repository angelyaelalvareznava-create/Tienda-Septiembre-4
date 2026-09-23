package com.example.tiendita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.ui.navigation.NexoNavGraph
import com.example.tiendita.ui.theme.NexoStockTheme
import com.example.tiendita.viewmodel.UserViewModel
import com.example.tiendita.viewmodel.UserViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NexoStockTheme(dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val database = remember { AppDatabase.getDatabase(context.applicationContext) }
                    val repository = remember { com.example.tiendita.repository.UserRepositoryImpl(database.userDao()) }
                    
                    val viewModel: UserViewModel = viewModel(
                        factory = UserViewModelFactory(repository)
                    )
                    
                    NexoNavGraph(viewModel = viewModel)
                }
            }
        }
    }
}
