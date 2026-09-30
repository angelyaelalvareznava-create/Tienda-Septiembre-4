package com.example.tiendita.repository

import androidx.room.withTransaction
import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.data.local.model.*
import com.example.tiendita.database.AppDatabase
import kotlinx.coroutines.CancellationException

sealed interface DatabaseStatusResult {
    data class Success(val snapshot: DatabaseSnapshot) : DatabaseStatusResult
    data object Unauthorized : DatabaseStatusResult
    data object Error : DatabaseStatusResult
}
interface DatabaseStatusRepository { suspend fun load(actorId: Long): DatabaseStatusResult }
class RoomDatabaseStatusRepository(private val database: AppDatabase) : DatabaseStatusRepository {
    override suspend fun load(actorId: Long): DatabaseStatusResult = try {
        database.withTransaction {
            val actor = database.accountDao().getAccountById(actorId)
            if (actor?.active != true || actor.role != AccountRole.ADMIN) DatabaseStatusResult.Unauthorized
            else DatabaseStatusResult.Success(DatabaseSnapshot(database.databaseStatusDao().counts(),
                database.databaseStatusDao().accountSummaries()))
        }
    } catch (e: CancellationException) { throw e }
    catch (_: Exception) { DatabaseStatusResult.Error }
}
