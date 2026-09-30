package com.example.tiendita.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.tiendita.data.local.model.*

@Dao
interface DatabaseStatusDao {
    @Query("SELECT (SELECT COUNT(*) FROM user_accounts) AS accounts, " +
        "(SELECT COUNT(*) FROM user_accounts WHERE active = 1 AND role = 'ADMIN') AS administrators, " +
        "(SELECT COUNT(*) FROM employees) AS employees, (SELECT COUNT(*) FROM clients) AS clients, " +
        "(SELECT COUNT(*) FROM suppliers) AS suppliers, (SELECT COUNT(*) FROM categories) AS categories, " +
        "(SELECT COUNT(*) FROM products) AS products, (SELECT COUNT(*) FROM warehouses) AS warehouses, " +
        "(SELECT COUNT(*) FROM stock) AS stock, (SELECT COUNT(*) FROM inventory_movements) AS movements, " +
        "(SELECT COUNT(*) FROM calendar_events) AS events")
    suspend fun counts(): DatabaseCounts

    @Query("SELECT username, display_name, role, active, employee_id FROM user_accounts ORDER BY normalized_username")
    suspend fun accountSummaries(): List<AccountSummary>
}
