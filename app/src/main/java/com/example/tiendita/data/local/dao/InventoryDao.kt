package com.example.tiendita.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.tiendita.data.local.converter.MovementType
import com.example.tiendita.data.local.entity.InventoryMovementEntity
import com.example.tiendita.data.local.entity.MovementLineEntity
import com.example.tiendita.data.local.entity.StockEntity

@Dao
interface InventoryDao {
    @Insert
    suspend fun insertMovement(movement: InventoryMovementEntity): Long

    @Insert
    suspend fun insertMovementLine(line: MovementLineEntity): Long

    @Query("SELECT * FROM inventory_movements WHERE id = :id")
    suspend fun getMovementById(id: Long): InventoryMovementEntity?

    @Query("SELECT * FROM movement_lines WHERE movement_id = :movementId")
    suspend fun getLinesForMovement(movementId: Long): List<MovementLineEntity>

    @Query("SELECT quantity FROM stock WHERE product_id = :productId AND warehouse_id = :warehouseId")
    suspend fun getStockQuantity(productId: Long, warehouseId: Long): Long?

    @Query("UPDATE stock SET quantity = :quantity, updated_at = :updatedAt WHERE product_id = :productId AND warehouse_id = :warehouseId")
    suspend fun updateStockQuantity(productId: Long, warehouseId: Long, quantity: Long, updatedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceStock(stock: StockEntity)

    @Transaction
    suspend fun executeMovement(movement: InventoryMovementEntity, lines: List<MovementLineEntity>) {
        // 1. Validate movement rules
        when (movement.type) {
            MovementType.IN -> {
                require(movement.destinationWarehouseId != null) { "IN requires destination warehouse" }
                require(movement.supplierId != null) { "IN requires supplier" }
                require(movement.clientId == null) { "IN does not use client" }
                require(movement.originWarehouseId == null) { "IN does not use origin warehouse" }
            }
            MovementType.OUT -> {
                require(movement.originWarehouseId != null) { "OUT requires origin warehouse" }
                require(movement.clientId != null) { "OUT requires client" }
                require(movement.supplierId == null) { "OUT does not use supplier" }
                require(movement.destinationWarehouseId == null) { "OUT does not use destination warehouse" }
            }
            MovementType.TRANSFER -> {
                require(movement.originWarehouseId != null) { "TRANSFER requires origin warehouse" }
                require(movement.destinationWarehouseId != null) { "TRANSFER requires destination warehouse" }
                require(movement.originWarehouseId != movement.destinationWarehouseId) { "Origin and destination warehouses must be different" }
                require(movement.clientId == null) { "TRANSFER does not use client" }
                require(movement.supplierId == null) { "TRANSFER does not use supplier" }
            }
            MovementType.ADJ_IN -> {
                require(movement.destinationWarehouseId != null) { "ADJ_IN requires destination warehouse" }
                require(movement.originWarehouseId == null) { "ADJ_IN does not use origin warehouse" }
                require(movement.supplierId == null) { "ADJ_IN does not use supplier" }
                require(movement.clientId == null) { "ADJ_IN does not use client" }
            }
            MovementType.ADJ_OUT -> {
                require(movement.originWarehouseId != null) { "ADJ_OUT requires origin warehouse" }
                require(movement.destinationWarehouseId == null) { "ADJ_OUT does not use destination warehouse" }
                require(movement.supplierId == null) { "ADJ_OUT does not use supplier" }
                require(movement.clientId == null) { "ADJ_OUT does not use client" }
            }
        }

        for (line in lines) {
            require(line.quantity > 0) { "Movement line quantity must be greater than zero" }
        }

        // 2. Insert movement header
        val movementId = insertMovement(movement)

        // 3. Process lines and stock changes atomically
        val now = System.currentTimeMillis()
        for (line in lines) {
            val lineWithMovement = line.copy(movementId = movementId)
            insertMovementLine(lineWithMovement)

            when (movement.type) {
                MovementType.IN, MovementType.ADJ_IN -> {
                    val whId = movement.destinationWarehouseId!!
                    val currentStock = getStockQuantity(line.productId, whId) ?: 0L
                    val newStock = currentStock + line.quantity
                    updateOrInsertStock(line.productId, whId, newStock, now)
                }
                MovementType.OUT, MovementType.ADJ_OUT -> {
                    val whId = movement.originWarehouseId!!
                    val currentStock = getStockQuantity(line.productId, whId) ?: 0L
                    require(currentStock >= line.quantity) { "Insufficient stock: cannot result in negative inventory" }
                    val newStock = currentStock - line.quantity
                    updateOrInsertStock(line.productId, whId, newStock, now)
                }
                MovementType.TRANSFER -> {
                    val origWhId = movement.originWarehouseId!!
                    val destWhId = movement.destinationWarehouseId!!
                    
                    val origStock = getStockQuantity(line.productId, origWhId) ?: 0L
                    require(origStock >= line.quantity) { "Insufficient stock at origin warehouse for transfer" }
                    val newOrigStock = origStock - line.quantity
                    updateOrInsertStock(line.productId, origWhId, newOrigStock, now)

                    val destStock = getStockQuantity(line.productId, destWhId) ?: 0L
                    val newDestStock = destStock + line.quantity
                    updateOrInsertStock(line.productId, destWhId, newDestStock, now)
                }
            }
        }
    }

    private suspend fun updateOrInsertStock(productId: Long, warehouseId: Long, quantity: Long, updatedAt: Long) {
        val existing = getStockQuantity(productId, warehouseId)
        if (existing != null) {
            updateStockQuantity(productId, warehouseId, quantity, updatedAt)
        } else {
            insertOrReplaceStock(StockEntity(productId = productId, warehouseId = warehouseId, quantity = quantity, updatedAt = updatedAt))
        }
    }
}
