package com.example.tiendita.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tiendita.data.local.entity.AuthMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthMetadataDao {
    @Query("SELECT * FROM auth_metadata WHERE id = 1")
    suspend fun getState(): AuthMetadataEntity?

    @Query("SELECT * FROM auth_metadata WHERE id = 1")
    fun observeState(): Flow<AuthMetadataEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInitialState(state: AuthMetadataEntity)

    /** Must be called inside the future administrator creation transaction. Never reopens bootstrap. */
    @Query("UPDATE auth_metadata SET initial_admin_created = 1, updated_at = :updatedAt " +
        "WHERE id = 1 AND initial_admin_created = 0")
    suspend fun markInitialAdminCreated(updatedAt: Long): Int
}
