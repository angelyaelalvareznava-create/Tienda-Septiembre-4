package com.example.tiendita.repository

import androidx.room.withTransaction
import com.example.tiendita.database.AppDatabase
import kotlinx.coroutines.flow.*

data class BootstrapStatus(val consumed: Boolean, val accounts: Int)
interface BootstrapRepository { fun observe(): Flow<BootstrapStatus> }
class RoomBootstrapRepository(private val database: AppDatabase) : BootstrapRepository {
    override fun observe(): Flow<BootstrapStatus> = combine(
        database.authMetadataDao().observeState(), database.accountDao().observeAccountCount()
    ) { _, _ -> database.withTransaction {
        BootstrapStatus(database.authMetadataDao().getState()?.initialAdminCreated == true, database.accountDao().countAccounts())
    } }
}
