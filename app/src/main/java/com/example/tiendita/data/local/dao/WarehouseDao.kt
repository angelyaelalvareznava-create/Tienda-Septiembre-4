package com.example.tiendita.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tiendita.data.local.entity.WarehouseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {
    @Insert
    suspend fun insertWarehouse(warehouse: WarehouseEntity): Long

    @Update
    suspend fun updateWarehouse(warehouse: WarehouseEntity)

    @Query("SELECT * FROM warehouses WHERE is_active = 1")
    fun getActiveWarehousesFlow(): Flow<List<WarehouseEntity>>

    @Query("SELECT COUNT(*) FROM warehouses")
    suspend fun countWarehouses(): Int
}
