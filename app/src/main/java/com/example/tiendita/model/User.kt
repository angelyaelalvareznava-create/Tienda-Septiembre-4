package com.example.tiendita.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa la tabla "users" en la base de datos local (Room).
 *
 * @property id Clave primaria autoincrementable, generada por Room.
 * @property nombre Nombre del usuario.
 * @property apellidos Apellidos del usuario.
 * @property direccion Dirección del usuario.
 * @property telefono Teléfono de contacto del usuario.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val apellidos: String,
    val direccion: String,
    val telefono: String
)
