package com.example.tiendita.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tiendita.data.local.converter.ProductUnit

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["sku"], unique = true),
        Index(value = ["barcode"], unique = true),
        Index(value = ["category_id"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val sku: String,
    
    val barcode: String?,
    
    val name: String,
    
    val description: String?,
    
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    
    @ColumnInfo(defaultValue = "PIECE")
    val unit: ProductUnit,
    
    @ColumnInfo(name = "purchase_price")
    val purchasePrice: Long,
    
    @ColumnInfo(name = "sale_price")
    val salePrice: Long?,
    
    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
