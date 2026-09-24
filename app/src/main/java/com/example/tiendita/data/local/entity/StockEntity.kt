package com.example.tiendita.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "stock",
    primaryKeys = ["product_id", "warehouse_id"],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = WarehouseEntity::class,
            parentColumns = ["id"],
            childColumns = ["warehouse_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["warehouse_id"])
    ]
)
data class StockEntity(
    @ColumnInfo(name = "product_id")
    val productId: Long,
    
    @ColumnInfo(name = "warehouse_id")
    val warehouseId: Long,
    
    val quantity: Long,
    
    @ColumnInfo(name = "min_quantity", defaultValue = "0")
    val minQuantity: Long = 0,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
