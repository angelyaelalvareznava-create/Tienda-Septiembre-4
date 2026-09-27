package com.example.tiendita.repository

import com.example.tiendita.data.local.entity.ClientEntity
import com.example.tiendita.data.local.entity.EmployeeEntity
import com.example.tiendita.data.local.entity.SupplierEntity
import com.example.tiendita.database.AppDatabase
import com.example.tiendita.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface UserRepository {
    suspend fun insertUser(user: User): Long
    suspend fun getAllUsers(): List<User>
    suspend fun insertClient(client: ClientEntity): Long
    suspend fun insertEmployee(employee: EmployeeEntity): Long
    suspend fun insertSupplier(supplier: SupplierEntity): Long
}

class UserRepositoryImpl(private val database: AppDatabase) : UserRepository {

    override suspend fun insertUser(user: User): Long {
        return withContext(Dispatchers.IO) {
            database.userDao().insertUser(user)
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            database.userDao().getAllUsers()
        }
    }

    override suspend fun insertClient(client: ClientEntity): Long {
        return withContext(Dispatchers.IO) {
            database.clientDao().insertClient(client)
        }
    }

    override suspend fun insertEmployee(employee: EmployeeEntity): Long {
        return withContext(Dispatchers.IO) {
            database.employeeDao().insertEmployee(employee)
        }
    }

    override suspend fun insertSupplier(supplier: SupplierEntity): Long {
        return withContext(Dispatchers.IO) {
            database.supplierDao().insertSupplier(supplier)
        }
    }
}
