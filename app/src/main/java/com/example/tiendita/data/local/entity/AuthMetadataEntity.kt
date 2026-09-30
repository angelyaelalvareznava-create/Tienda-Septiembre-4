package com.example.tiendita.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Global bootstrap state; never stored in session preferences. */
@Entity(tableName = "auth_metadata")
data class AuthMetadataEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    @ColumnInfo(name = "initial_admin_created") val initialAdminCreated: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = createdAt
) {
    init {
        require(id == SINGLETON_ID) { "Auth metadata must use the singleton ID" }
    }

    companion object {
        const val SINGLETON_ID = 1
    }
}
