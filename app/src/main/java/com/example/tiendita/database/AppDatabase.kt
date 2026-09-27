package com.example.tiendita.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tiendita.dao.UserDao
import com.example.tiendita.data.local.converter.EnumConverters
import com.example.tiendita.data.local.dao.AccountDao
import com.example.tiendita.data.local.dao.CalendarDao
import com.example.tiendita.data.local.dao.ClientDao
import com.example.tiendita.data.local.dao.EmployeeDao
import com.example.tiendita.data.local.dao.InventoryDao
import com.example.tiendita.data.local.dao.ProductDao
import com.example.tiendita.data.local.dao.SupplierDao
import com.example.tiendita.data.local.dao.WarehouseDao
import com.example.tiendita.data.local.entity.CalendarEventEntity
import com.example.tiendita.data.local.entity.CategoryEntity
import com.example.tiendita.data.local.entity.ClientEntity
import com.example.tiendita.data.local.entity.EmployeeEntity
import com.example.tiendita.data.local.entity.InventoryMovementEntity
import com.example.tiendita.data.local.entity.MovementLineEntity
import com.example.tiendita.data.local.entity.ProductEntity
import com.example.tiendita.data.local.entity.StockEntity
import com.example.tiendita.data.local.entity.SupplierEntity
import com.example.tiendita.data.local.entity.SupplierProductEntity
import com.example.tiendita.data.local.entity.UserAccountEntity
import com.example.tiendita.data.local.entity.WarehouseEntity
import com.example.tiendita.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        UserAccountEntity::class,
        EmployeeEntity::class,
        ClientEntity::class,
        SupplierEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        SupplierProductEntity::class,
        WarehouseEntity::class,
        StockEntity::class,
        InventoryMovementEntity::class,
        MovementLineEntity::class,
        CalendarEventEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun clientDao(): ClientDao
    abstract fun supplierDao(): SupplierDao
    abstract fun productDao(): ProductDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun calendarDao(): CalendarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gameshelf_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    prePopulateDatabase(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prePopulateDatabase(database: AppDatabase) {
            try {
                // 1. Clientes (3 examples)
                val clientDao = database.clientDao()
                if (clientDao.getActiveClientsFlow().first().isEmpty()) {
                    clientDao.insertClient(ClientEntity(name = "Abarrotes La Esquina", contactName = "Juan Pérez", phone = "3312345678", email = "juan@esquina.com", address = "Av. Juárez 100, GDL", notes = "Cliente frecuente"))
                    clientDao.insertClient(ClientEntity(name = "Café del Parque", contactName = "María Gómez", phone = "3387654321", email = "maria@cafe.com", address = "Calle Hidalgo 250, Zapopan", notes = "Servicio de cafetería"))
                    clientDao.insertClient(ClientEntity(name = "Mini Súper Zapopan", contactName = "Carlos Ruiz", phone = "3355443322", email = "carlos@super.com", address = "Blvd. Rosales 45, Tlaquepaque", notes = "Compra de mayoreo"))
                }

                // 2. Empleados (3 examples)
                val employeeDao = database.employeeDao()
                if (employeeDao.getActiveEmployeesFlow().first().isEmpty()) {
                    employeeDao.insertEmployee(EmployeeEntity(firstName = "Ana", lastName = "Torres", position = "Encargado", email = "ana.torres@nexostock.com", phone = "3311223344", address = "Av. Vallarta 1200, GDL", hireDate = System.currentTimeMillis(), active = true))
                    employeeDao.insertEmployee(EmployeeEntity(firstName = "Luis", lastName = "Mendoza", position = "Auxiliar", email = "luis.mendoza@nexostock.com", phone = "3399887766", address = "Paseo de los Virreyes 88, Zapopan", hireDate = System.currentTimeMillis(), active = true))
                    employeeDao.insertEmployee(EmployeeEntity(firstName = "Karla", lastName = "Ramírez", position = "Administración", email = "karla.ramirez@nexostock.com", phone = "3344556677", address = "Periférico Sur 500, Tlaquepaque", hireDate = System.currentTimeMillis(), active = true))
                }

                // 3. Proveedores (3 examples)
                val supplierDao = database.supplierDao()
                if (supplierDao.getActiveSuppliersFlow().first().isEmpty()) {
                    supplierDao.insertSupplier(SupplierEntity(companyName = "Distribuidora Occidente", contactName = "Pedro Sánchez", phone = "3322334455", email = "pedro@occidente.com", address = "Zona Industrial 300, GDL", notes = "Abarrotes y generales"))
                    supplierDao.insertSupplier(SupplierEntity(companyName = "Empaques del Centro", contactName = "Sofia Morales", phone = "3377665544", email = "sofia@empaques.com", address = "Av. Patria 1500, Zapopan", notes = "Material de embalaje"))
                    supplierDao.insertSupplier(SupplierEntity(companyName = "Limpieza Industrial GDL", contactName = "Javier Ortiz", phone = "3366554433", email = "javier@limpieza.com", address = "Carretera a Chapala km 12, Toluquilla", notes = "Productos de limpieza"))
                }

                // 4. Inventario / Productos (3 examples)
                val productDao = database.productDao()
                val catId = productDao.insertCategory(CategoryEntity(name = "Abarrotes", description = "Productos de despensa general"))
                val whDao = database.warehouseDao()
                val whId = whDao.insertWarehouse(WarehouseEntity(name = "Almacén Central", address = "Av. Principal 1000", description = "Bodega principal"))

                if (productDao.getActiveProductsFlow().first().isEmpty()) {
                    val p1 = productDao.insertProduct(ProductEntity(sku = "SKU-ARR-001", barcode = "75010001", name = "Arroz Grano Superior (1kg)", description = "Saco de arroz seleccionado", categoryId = catId, purchasePriceCents = 2000, salePriceCents = 2850))
                    val p2 = productDao.insertProduct(ProductEntity(sku = "SKU-ACE-002", barcode = "75010002", name = "Aceite Vegetal 1L", description = "Botella de aceite comestible", categoryId = catId, purchasePriceCents = 3000, salePriceCents = 4200))
                    val p3 = productDao.insertProduct(ProductEntity(sku = "SKU-AZU-003", barcode = "75010003", name = "Azúcar Morena 1kg", description = "Azúcar de caña natural", categoryId = catId, purchasePriceCents = 2200, salePriceCents = 3100))

                    // Initial stock
                    val invDao = database.inventoryDao()
                    invDao.insertOrReplaceStock(StockEntity(productId = p1, warehouseId = whId, quantity = 15000, minimumQuantity = 1000))
                    invDao.insertOrReplaceStock(StockEntity(productId = p2, warehouseId = whId, quantity = 8000, minimumQuantity = 500))
                    invDao.insertOrReplaceStock(StockEntity(productId = p3, warehouseId = whId, quantity = 12000, minimumQuantity = 800))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
