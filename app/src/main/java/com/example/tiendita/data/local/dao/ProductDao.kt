package com.example.tiendita.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tiendita.data.local.entity.CategoryEntity
import com.example.tiendita.data.local.entity.ProductEntity
import com.example.tiendita.data.local.entity.SupplierProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long

    @Query("SELECT * FROM categories WHERE is_active = 1")
    fun getActiveCategoriesFlow(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE sku = :sku")
    suspend fun getProductBySku(sku: String): ProductEntity?

    @Query("SELECT * FROM products WHERE is_active = 1")
    fun getActiveProductsFlow(): Flow<List<ProductEntity>>

    @Insert
    suspend fun insertSupplierProduct(supplierProduct: SupplierProductEntity)
}
