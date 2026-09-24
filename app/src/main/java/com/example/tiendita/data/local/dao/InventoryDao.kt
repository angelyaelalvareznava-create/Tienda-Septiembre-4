package com.example.tiendita.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tiendita.data.local.entity.InventoryMovementEntity
import com.example.tiendita.data.local.entity.MovementLineEntity

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
}
