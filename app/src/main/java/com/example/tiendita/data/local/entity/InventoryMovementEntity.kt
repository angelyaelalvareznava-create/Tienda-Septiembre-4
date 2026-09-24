package com.example.tiendita.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tiendita.data.local.converter.MovementType

@Entity(
    tableName = "inventory_movements",
    foreignKeys = [
        ForeignKey(
            entity = WarehouseEntity::class,
            parentColumns = ["id"],
            childColumns = ["origin_wh_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = WarehouseEntity::class,
            parentColumns = ["id"],
            childColumns = ["dest_wh_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplier_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["client_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["origin_wh_id"]),
        Index(value = ["dest_wh_id"]),
        Index(value = ["supplier_id"]),
        Index(value = ["client_id"]),
        Index(value = ["account_id"]),
        Index(value = ["effective_date"])
    ]
)
data class InventoryMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val type: MovementType,
    
    @ColumnInfo(name = "origin_wh_id")
    val originWhId: Long?,
    
    @ColumnInfo(name = "dest_wh_id")
    val destWhId: Long?,
    
    @ColumnInfo(name = "supplier_id")
    val supplierId: Long?,
    
    @ColumnInfo(name = "client_id")
    val clientId: Long?,
    
    @ColumnInfo(name = "account_id")
    val accountId: Long,
    
    val reference: String?,
    
    val notes: String?,
    
    @ColumnInfo(name = "effective_date")
    val effectiveDate: Long,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
