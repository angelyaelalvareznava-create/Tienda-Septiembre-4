package com.example.tiendita.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movement_lines",
    foreignKeys = [
        ForeignKey(
            entity = InventoryMovementEntity::class,
            parentColumns = ["id"],
            childColumns = ["movement_id"],
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
        Index(value = ["movement_id", "product_id"], unique = true),
        Index(value = ["product_id"])
    ]
)
data class MovementLineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "movement_id")
    val movementId: Long,
    
    @ColumnInfo(name = "product_id")
    val productId: Long,
    
    val quantity: Long,
    
    @ColumnInfo(name = "unit_cost")
    val unitCost: Long?
)
