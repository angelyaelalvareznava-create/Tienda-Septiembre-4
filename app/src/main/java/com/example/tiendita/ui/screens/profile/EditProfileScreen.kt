package com.example.tiendita.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.tiendita.R
import com.example.tiendita.ui.screens.common.PublicStatusScreen

@Composable fun EditProfileScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    PublicStatusScreen(stringResource(R.string.title_edit_profile), message = stringResource(R.string.auth_maintenance), onBack = onBack)
}
