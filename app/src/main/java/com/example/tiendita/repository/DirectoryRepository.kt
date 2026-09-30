package com.example.tiendita.repository

import com.example.tiendita.database.AppDatabase
import com.example.tiendita.ui.model.DirectoryItemUi
import kotlinx.coroutines.flow.*

enum class DirectoryKind { EMPLOYEES, CLIENTS, SUPPLIERS }
class DirectoryRepository(private val database: AppDatabase) {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observe(kind: DirectoryKind, actorId: Long): Flow<List<DirectoryItemUi>> {
        val items = when (kind) {
            DirectoryKind.EMPLOYEES -> database.employeeDao().getActiveEmployeesFlow().map { rows -> rows.filter { it.active }.map {
                val name = "${it.firstName} ${it.lastName}"
                DirectoryItemUi(it.id.toString(), name, it.position, name.take(2).uppercase())
            } }
            DirectoryKind.CLIENTS -> database.clientDao().getActiveClientsFlow().map { rows -> rows.filter { it.active }.map {
                DirectoryItemUi(it.id.toString(), it.name, it.address.orEmpty(), it.name.take(2).uppercase())
            } }
            DirectoryKind.SUPPLIERS -> database.supplierDao().getActiveSuppliersFlow().map { rows -> rows.filter { it.active }.map {
                DirectoryItemUi(it.id.toString(), it.companyName, it.address.orEmpty(), it.companyName.take(2).uppercase())
            } }
        }
        return database.accountDao().observeAccountById(actorId).flatMapLatest { actor ->
            if (actor?.active == true) items else flowOf(emptyList())
        }
    }
}
