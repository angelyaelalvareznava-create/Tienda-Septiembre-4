package com.example.tiendita.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tiendita.data.local.dao.DatabaseStatusDao
import com.example.tiendita.data.local.dao.AuthMetadataDao
import com.example.tiendita.data.local.entity.AuthMetadataEntity
import com.example.tiendita.dao.UserDao
import com.example.tiendita.data.local.converter.EnumConverters
import com.example.tiendita.data.local.dao.AccountDao
import com.example.tiendita.data.local.dao.AdminDao
import com.example.tiendita.data.local.dao.CalendarDao
import com.example.tiendita.data.local.dao.ClientDao
import com.example.tiendita.data.local.dao.EmployeeDao
import com.example.tiendita.data.local.dao.InventoryDao
import com.example.tiendita.data.local.dao.ProductDao
import com.example.tiendita.data.local.dao.SupplierDao
import com.example.tiendita.data.local.dao.WarehouseDao
import com.example.tiendita.data.local.entity.AdminEntity
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
        AuthMetadataEntity::class,
        EmployeeEntity::class,
        AdminEntity::class,
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
    version = 10,
    exportSchema = true
)
@TypeConverters(EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun databaseStatusDao(): DatabaseStatusDao
    abstract fun authMetadataDao(): AuthMetadataDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun adminDao(): AdminDao
    abstract fun clientDao(): ClientDao
    abstract fun supplierDao(): SupplierDao
    abstract fun productDao(): ProductDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun calendarDao(): CalendarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Also used by isolated database tests, so they exercise the production opening policy.
        internal fun buildDatabase(context: Context, name: String): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name)
                // V9 was preproduction test data: its reset was explicitly authorized.
                // Subsequent versions require explicit migrations.
                .fallbackToDestructiveMigrationFrom(true, 9)
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        // Room annotations cannot express CHECK(id = 1). Enforce it for raw writes too.
                        db.execSQL("CREATE TRIGGER IF NOT EXISTS auth_metadata_singleton_insert " +
                            "BEFORE INSERT ON auth_metadata WHEN NEW.id <> 1 " +
                            "BEGIN SELECT RAISE(ABORT, 'Invalid auth metadata singleton ID'); END")
                        db.execSQL("CREATE TRIGGER IF NOT EXISTS auth_metadata_singleton_update " +
                            "BEFORE UPDATE OF id ON auth_metadata WHEN NEW.id <> 1 " +
                            "BEGIN SELECT RAISE(ABORT, 'Invalid auth metadata singleton ID'); END")
                    }
                })
                .build()

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context, "gameshelf_database")
                INSTANCE = instance
                instance
            }
        }
    }
}
