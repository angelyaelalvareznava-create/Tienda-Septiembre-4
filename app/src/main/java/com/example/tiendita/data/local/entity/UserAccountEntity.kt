package com.example.tiendita.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tiendita.data.local.converter.AccountRole

@Entity(
    tableName = "user_accounts",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employee_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["employee_id"], unique = true)
    ]
)
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val username: String,
    
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    
    val salt: String,
    
    @ColumnInfo(name = "password_algorithm")
    val passwordAlgorithm: String,
    
    @ColumnInfo(name = "password_iterations")
    val passwordIterations: Int,
    
    @ColumnInfo(name = "display_name")
    val displayName: String,
    
    val email: String?,
    
    val phone: String?,
    
    val role: AccountRole,
    
    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "employee_id")
    val employeeId: Long?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
