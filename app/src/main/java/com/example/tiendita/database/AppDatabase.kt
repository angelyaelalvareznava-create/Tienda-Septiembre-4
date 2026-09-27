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
import com.example.tiendita.data.local.dao.InventoryItemDao
import com.example.tiendita.data.local.dao.ProductDao
import com.example.tiendita.data.local.dao.SupplierDao
import com.example.tiendita.data.local.dao.WarehouseDao
import com.example.tiendita.data.local.entity.CalendarEventEntity
import com.example.tiendita.data.local.entity.CategoryEntity
import com.example.tiendita.data.local.entity.ClientEntity
import com.example.tiendita.data.local.entity.EmployeeEntity
import com.example.tiendita.data.local.entity.InventoryItemEntity
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
        InventoryItemEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        SupplierProductEntity::class,
        WarehouseEntity::class,
        StockEntity::class,
        InventoryMovementEntity::class,
        MovementLineEntity::class,
        CalendarEventEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun clientDao(): ClientDao
    abstract fun supplierDao(): SupplierDao
    abstract fun inventoryItemDao(): InventoryItemDao
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
                    .fallbackToDestructiveMigration()
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
                    clientDao.insertClient(
                        ClientEntity(firstName = "Juan", lastName = "Pérez", phone = "3312345678", address = "Av. Juárez 100, GDL", email = "juan.perez@example.com")
                    )
                    clientDao.insertClient(
                        ClientEntity(firstName = "María", lastName = "Gómez", phone = "3387654321", address = "Calle Hidalgo 250, Zapopan", email = "maria.gomez@example.com")
                    )
                    clientDao.insertClient(
                        ClientEntity(firstName = "Carlos", lastName = "Ruiz", phone = "3355443322", address = "Blvd. Rosales 45, Tlaquepaque", email = "carlos.ruiz@example.com")
                    )
                }

                // 2. Empleados (3 examples)
                val employeeDao = database.employeeDao()
                if (employeeDao.getActiveEmployeesFlow().first().isEmpty()) {
                    employeeDao.insertEmployee(
                        EmployeeEntity(firstName = "Ana", lastName = "Torres", position = "Encargada", phone = "3311223344", address = "Av. Vallarta 1200, GDL", email = "ana.torres@nexostock.com", hireDate = System.currentTimeMillis())
                    )
                    employeeDao.insertEmployee(
                        EmployeeEntity(firstName = "Luis", lastName = "Mendoza", position = "Auxiliar", phone = "3399887766", address = "Paseo de los Virreyes 88, Zapopan", email = "luis.mendoza@nexostock.com", hireDate = System.currentTimeMillis())
                    )
                    employeeDao.insertEmployee(
                        EmployeeEntity(firstName = "Karla", lastName = "Ramírez", position = "Administración", phone = "3344556677", address = "Periférico Sur 500, Tlaquepaque", email = "karla.ramirez@nexostock.com", hireDate = System.currentTimeMillis())
                    )
                }

                // 3. Proveedores (3 examples)
                val supplierDao = database.supplierDao()
                if (supplierDao.getActiveSuppliersFlow().first().isEmpty()) {
                    supplierDao.insertSupplier(
                        SupplierEntity(firstName = "Pedro", lastName = "Sánchez", phone = "3322334455", address = "Zona Industrial 300, GDL", email = "pedro.sanchez@proveedores.com")
                    )
                    supplierDao.insertSupplier(
                        SupplierEntity(firstName = "Sofia", lastName = "Morales", phone = "3377665544", address = "Av. Patria 1500, Zapopan", email = "sofia.morales@distribuidora.com")
                    )
                    supplierDao.insertSupplier(
                        SupplierEntity(firstName = "Javier", lastName = "Ortiz", phone = "3366554433", address = "Carretera a Chapala km 12, Toluquilla", email = "javier.ortiz@mayoreo.com")
                    )
                }

                // 4. Inventario (3 examples)
                val inventoryItemDao = database.inventoryItemDao()
                if (inventoryItemDao.getAllItems().isEmpty()) {
                    inventoryItemDao.insertItem(
                        InventoryItemEntity(name = "Arroz Grano Superior (1kg)", description = "Saco de arroz seleccionado", quantity = 150, price = 28.50)
                    )
                    inventoryItemDao.insertItem(
                        InventoryItemEntity(name = "Aceite Vegetal 1L", description = "Botella de aceite comestible", quantity = 80, price = 42.00)
                    )
                    inventoryItemDao.insertItem(
                        InventoryItemEntity(name = "Azúcar Morena 1kg", description = "Azúcar de caña natural", quantity = 120, price = 31.00)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
