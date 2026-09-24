package com.example.tiendita.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 2,
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
                    .fallbackToDestructiveMigrationFrom(true, 1)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
