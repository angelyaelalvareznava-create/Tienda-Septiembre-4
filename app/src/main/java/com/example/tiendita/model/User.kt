package com.example.tiendita.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val nombre: String,
    val apellidos: String,
    val direccion: String = "",
    val telefono: String,
    val email: String,
    val password: String,
    val userType: String = "Empleado" // "Admin", "Empleado", "Cliente", "Proveedor"
)
