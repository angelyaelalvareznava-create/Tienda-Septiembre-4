package com.example.tiendita.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.util.TableInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.tiendita.data.local.entity.AuthMetadataEntity
import com.example.tiendita.database.AppDatabase
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class V9ResetToV10Test {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun productionPolicyResetsAllV9TablesAndValidatesV10() = runBlocking {
        val name = "v9-reset-${UUID.randomUUID()}"
        val oldSchema = schema(9).getJSONArray("entities")
        val tables = (0 until oldSchema.length()).map { oldSchema.getJSONObject(it) }
        try {
            createFromExportedSchema(name, 9).apply {
                // Isolated synthetic V9 fixtures. No installed application database is opened here.
                execSQL("PRAGMA foreign_keys = OFF")
                tables.forEach { entity ->
                    val fields = entity.getJSONArray("fields")
                    val columns = (0 until fields.length()).map { fields.getJSONObject(it) }
                    val values = columns.map<JSONObject, Any> { field ->
                        if (field.getString("affinity") == "INTEGER") 1L else "v9-test-fixture"
                    }.toTypedArray()
                    execSQL("INSERT INTO `${entity.getString("tableName")}` (" +
                        columns.joinToString { "`${it.getString("columnName")}`" } + ") VALUES (" +
                        columns.joinToString { "?" } + ")", values)
                }
                close()
            }
            val db = AppDatabase.buildDatabase(context, name)
            try {
                assertEquals(10, db.openHelper.writableDatabase.version)
                tables.forEach { entity ->
                    db.openHelper.readableDatabase.query(
                        "SELECT COUNT(*) FROM `${entity.getString("tableName")}`").use {
                        assertTrue(it.moveToFirst())
                        assertEquals(0, it.getInt(0))
                    }
                }
                assertEquals(0, db.accountDao().countAccounts())
                assertNull(db.authMetadataDao().getState())
                db.authMetadataDao().insertInitialState(AuthMetadataEntity(createdAt = 10))
                assertFalse(db.authMetadataDao().getState()!!.initialAdminCreated)
            } finally { db.close() }
            validateAgainstExportedV10(name)
        } finally { context.deleteDatabase(name) }
    }

    @Test fun missingPathFromV8FailsWithoutDeletingData() = runBlocking {
        rejectUnsupportedVersion(8)
    }

    @Test fun downgradeFromV11FailsWithoutDeletingData() = runBlocking {
        rejectUnsupportedVersion(11)
    }

    private fun schema(version: Int): JSONObject =
        InstrumentationRegistry.getInstrumentation().context.assets.open(
            "com.example.tiendita.database.AppDatabase/$version.json").bufferedReader().use {
            JSONObject(it.readText()).getJSONObject("database")
        }

    // MigrationTestHelper's schema serializer currently throws AbstractMethodError on this runtime.
    // Execute the immutable exported SQL instead; use Room's TableInfo to validate every V10 table.
    private fun createFromExportedSchema(name: String, version: Int): SQLiteDatabase {
        val exported = schema(version)
        val path = context.getDatabasePath(name)
        path.parentFile!!.mkdirs()
        return SQLiteDatabase.openOrCreateDatabase(path, null).apply {
            val entities = exported.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices")
                if (indices != null) for (j in 0 until indices.length()) {
                    execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
                }
            }
            val setup = exported.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) execSQL(setup.getString(i))
            this.version = version
        }
    }

    private fun validateAgainstExportedV10(name: String) {
        val expectedName = "expected-v10-${UUID.randomUUID()}"
        try {
            createFromExportedSchema(expectedName, 10).close()
            val actual = AppDatabase.buildDatabase(context, name)
            val expected = AppDatabase.buildDatabase(context, expectedName)
            try {
                val entities = schema(10).getJSONArray("entities")
                for (i in 0 until entities.length()) {
                    val table = entities.getJSONObject(i).getString("tableName")
                    assertEquals(table, TableInfo.read(expected.openHelper.readableDatabase, table),
                        TableInfo.read(actual.openHelper.readableDatabase, table))
                }
            } finally { actual.close(); expected.close() }
        } finally { context.deleteDatabase(expectedName) }
    }

    private suspend fun rejectUnsupportedVersion(version: Int) {
        val name = "unsupported-version-${UUID.randomUUID()}"
        try {
            AppDatabase.buildDatabase(context, name).apply {
                openHelper.writableDatabase.execSQL(
                    "INSERT INTO users (username,nombre,apellidos,direccion,telefono,email,password,userType) " +
                        "VALUES ('fixture','test','test','','','','','Cliente')")
                openHelper.writableDatabase.version = version
                close()
            }
            val db = AppDatabase.buildDatabase(context, name)
            try {
                try {
                    db.openHelper.writableDatabase
                    fail("Unsupported version was opened destructively")
                } catch (_: IllegalStateException) { }
            } finally { db.close() }
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READONLY).use {
                assertEquals(version, it.version)
                it.rawQuery("SELECT COUNT(*) FROM users", null).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(1, cursor.getInt(0))
                }
            }
        } finally { context.deleteDatabase(name) }
    }
}
