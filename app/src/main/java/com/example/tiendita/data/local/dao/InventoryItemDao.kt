package com.example.tiendita.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tiendita.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemDao {
    @Insert
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Query("SELECT * FROM inventory_items")
    fun getAllItemsFlow(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items")
    suspend fun getAllItems(): List<InventoryItemEntity>
}
