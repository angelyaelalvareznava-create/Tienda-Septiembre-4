package com.example.tiendita.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "company_name")
    val companyName: String,
    
    @ColumnInfo(name = "contact_name")
    val contactName: String?,
    
    val phone: String?,
    
    val email: String?,
    
    val address: String?,
    
    val notes: String?,
    
    @ColumnInfo(defaultValue = "1")
    val active: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
