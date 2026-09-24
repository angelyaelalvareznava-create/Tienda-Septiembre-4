package com.example.tiendita.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "supplier_products",
    primaryKeys = ["supplier_id", "product_id"],
    foreignKeys = [
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplier_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["product_id"])
    ]
)
data class SupplierProductEntity(
    @ColumnInfo(name = "supplier_id")
    val supplierId: Long,
    
    @ColumnInfo(name = "product_id")
    val productId: Long,
    
    @ColumnInfo(name = "supplier_sku")
    val supplierSku: String?,
    
    @ColumnInfo(name = "last_cost")
    val lastCost: Long?,
    
    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
