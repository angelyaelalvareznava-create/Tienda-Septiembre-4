package com.example.tiendita.data.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tiendita.data.local.converter.AccountRole
import com.example.tiendita.data.local.converter.MovementType
import com.example.tiendita.data.local.converter.ProductUnit
import com.example.tiendita.data.local.entity.CategoryEntity
import com.example.tiendita.data.local.entity.EmployeeEntity
import com.example.tiendita.data.local.entity.InventoryMovementEntity
import com.example.tiendita.data.local.entity.MovementLineEntity
import com.example.tiendita.data.local.entity.ProductEntity
import com.example.tiendita.data.local.entity.StockEntity
import com.example.tiendita.data.local.entity.SupplierEntity
import com.example.tiendita.data.local.entity.SupplierProductEntity
import com.example.tiendita.data.local.entity.UserAccountEntity
import com.example.tiendita.data.local.entity.WarehouseEntity
import com.example.tiendita.database.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadAccount() {
        runBlocking {
            val account = UserAccountEntity(
                username = "admin",
                passwordHash = "hash",
                salt = "salt",
                passwordAlgorithm = "PBKDF2WithHmacSHA256",
                passwordIterations = 10000,
                displayName = "Admin",
                email = null,
                phone = null,
                role = AccountRole.ADMIN,
                employeeId = null,
                createdAt = 100L,
                updatedAt = 100L
            )
            val id = db.accountDao().insertAccount(account)
            val loaded = db.accountDao().getAccountById(id)
            assertNotNull(loaded)
            assertEquals("admin", loaded?.username)
        }
    }

    @Test(expected = SQLiteConstraintException::class)
    fun duplicateUsernameIsRejected() {
        runBlocking {
            val account1 = UserAccountEntity(username = "admin", passwordHash = "a", salt = "b", passwordAlgorithm = "c", passwordIterations = 1, displayName = "d", email = null, phone = null, role = AccountRole.ADMIN, employeeId = null, createdAt = 1, updatedAt = 1)
            val account2 = UserAccountEntity(username = "admin", passwordHash = "x", salt = "y", passwordAlgorithm = "z", passwordIterations = 1, displayName = "w", email = null, phone = null, role = AccountRole.ADMIN, employeeId = null, createdAt = 1, updatedAt = 1)
            db.accountDao().insertAccount(account1)
            db.accountDao().insertAccount(account2) // Should throw
        }
    }

    @Test(expected = SQLiteConstraintException::class)
    fun twoAccountsCannotShareSameEmployee() {
        runBlocking {
            val employee = EmployeeEntity(firstName = "Ana", lastName = "Perez", position = "Manager", email = null, phone = "123", address = null, hireDate = null, createdAt = 1L, updatedAt = 1L)
            val empId = db.employeeDao().insertEmployee(employee)

            val account1 = UserAccountEntity(username = "user1", passwordHash = "a", salt = "b", passwordAlgorithm = "c", passwordIterations = 1, displayName = "d", email = null, phone = null, role = AccountRole.ADMIN, employeeId = empId, createdAt = 1, updatedAt = 1)
            val account2 = UserAccountEntity(username = "user2", passwordHash = "x", salt = "y", passwordAlgorithm = "z", passwordIterations = 1, displayName = "w", email = null, phone = null, role = AccountRole.ADMIN, employeeId = empId, createdAt = 1, updatedAt = 1)

            db.accountDao().insertAccount(account1)
            db.accountDao().insertAccount(account2) // Should throw
        }
    }

    @Test(expected = SQLiteConstraintException::class)
    fun duplicateSkuIsRejected() {
        runBlocking {
            val catId = db.productDao().insertCategory(CategoryEntity(name = "Drinks", description = null, createdAt = 1, updatedAt = 1))
            val p1 = ProductEntity(sku = "SKU1", barcode = null, name = "Coke", description = null, categoryId = catId, unit = ProductUnit.PIECE, purchasePrice = 100, salePrice = 200, createdAt = 1, updatedAt = 1)
            val p2 = ProductEntity(sku = "SKU1", barcode = null, name = "Pepsi", description = null, categoryId = catId, unit = ProductUnit.PIECE, purchasePrice = 100, salePrice = 200, createdAt = 1, updatedAt = 1)

            db.productDao().insertProduct(p1)
            db.productDao().insertProduct(p2) // Should throw
        }
    }

    @Test(expected = SQLiteConstraintException::class)
    fun sameProductInMovementIsRejected() {
        runBlocking {
            val accountId = db.accountDao().insertAccount(UserAccountEntity(username = "admin", passwordHash = "a", salt = "b", passwordAlgorithm = "c", passwordIterations = 1, displayName = "d", email = null, phone = null, role = AccountRole.ADMIN, employeeId = null, createdAt = 1, updatedAt = 1))
            val catId = db.productDao().insertCategory(CategoryEntity(name = "Drinks", description = null, createdAt = 1, updatedAt = 1))
            val p1 = db.productDao().insertProduct(ProductEntity(sku = "SKU1", barcode = null, name = "Coke", description = null, categoryId = catId, unit = ProductUnit.PIECE, purchasePrice = 100, salePrice = 200, createdAt = 1, updatedAt = 1))

            val whId = db.warehouseDao().insertWarehouse(WarehouseEntity(name = "Main", address = null, createdAt = 1, updatedAt = 1))
            val movementId = db.inventoryDao().insertMovement(InventoryMovementEntity(type = MovementType.ADJ_IN, originWhId = null, destWhId = whId, supplierId = null, clientId = null, accountId = accountId, reference = null, notes = null, effectiveDate = 1, createdAt = 1))

            db.inventoryDao().insertMovementLine(MovementLineEntity(movementId = movementId, productId = p1, quantity = 5, unitCost = null))
            db.inventoryDao().insertMovementLine(MovementLineEntity(movementId = movementId, productId = p1, quantity = 10, unitCost = null)) // Should throw due to Unique(movement_id, product_id)
        }
    }

    @Test
    fun employeeDeleteSetsNullOnAccount() {
        runBlocking {
            val empId = db.employeeDao().insertEmployee(EmployeeEntity(firstName = "Ana", lastName = "Perez", position = "Manager", email = null, phone = "123", address = null, hireDate = null, createdAt = 1L, updatedAt = 1L))
            val accId = db.accountDao().insertAccount(UserAccountEntity(username = "admin", passwordHash = "a", salt = "b", passwordAlgorithm = "c", passwordIterations = 1, displayName = "d", email = null, phone = null, role = AccountRole.ADMIN, employeeId = empId, createdAt = 1, updatedAt = 1))

            // Access via raw query since EmployeeDao doesn't expose delete
            db.openHelper.writableDatabase.execSQL("DELETE FROM employees WHERE id = $empId")

            val account = db.accountDao().getAccountById(accId)
            assertEquals(null, account?.employeeId)
        }
    }

    @Test(expected = SQLiteConstraintException::class)
    fun productDeleteRestrictedWhenStockExists() {
        runBlocking {
            val catId = db.productDao().insertCategory(CategoryEntity(name = "Drinks", description = null, createdAt = 1, updatedAt = 1))
            val p1Id = db.productDao().insertProduct(ProductEntity(sku = "SKU1", barcode = null, name = "Coke", description = null, categoryId = catId, unit = ProductUnit.PIECE, purchasePrice = 100, salePrice = 200, createdAt = 1, updatedAt = 1))
            val whId = db.warehouseDao().insertWarehouse(WarehouseEntity(name = "Main", address = null, createdAt = 1, updatedAt = 1))

            // Insert directly using raw SQL since DAO doesn't have it yet
            db.openHelper.writableDatabase.execSQL("INSERT INTO stock (product_id, warehouse_id, quantity, min_quantity, updated_at) VALUES ($p1Id, $whId, 10, 0, 1)")

            // Try to delete product
            db.openHelper.writableDatabase.execSQL("DELETE FROM products WHERE id = $p1Id") // Should throw RESTRICT violation
        }
    }
}
