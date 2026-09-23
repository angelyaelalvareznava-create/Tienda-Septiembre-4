package com.example.tiendita.ui.model

/**
 * Modelo provisional exclusivo de presentación para los directorios.
 * No es una entidad de Room y no debe usarse para persistencia real.
 */
data class DirectoryItemUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val initials: String
)
