package com.example.tiendita.ui.screens.auth

import androidx.compose.runtime.Composable
import com.example.tiendita.viewmodel.AccountCreationViewModel
import com.example.tiendita.ui.screens.registration.AccountFormScreen

@Composable fun InitialAdminScreen(viewModel: AccountCreationViewModel) = AccountFormScreen(viewModel)
