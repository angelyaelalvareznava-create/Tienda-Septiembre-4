package com.example.tiendita.data.local.model

import androidx.room.ColumnInfo
import com.example.tiendita.data.local.converter.AccountRole

data class DatabaseCounts(val accounts: Int, val administrators: Int, val employees: Int, val clients: Int,
    val suppliers: Int, val categories: Int, val products: Int, val warehouses: Int, val stock: Int,
    val movements: Int, val events: Int)
/** Intentionally has no credential fields. */
data class AccountSummary(val username: String, @ColumnInfo(name = "display_name") val displayName: String?,
    val role: AccountRole, val active: Boolean, @ColumnInfo(name = "employee_id") val employeeId: Long?)
data class DatabaseSnapshot(val counts: DatabaseCounts, val accounts: List<AccountSummary>)
