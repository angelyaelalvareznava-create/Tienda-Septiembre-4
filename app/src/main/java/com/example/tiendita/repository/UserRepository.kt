package com.example.tiendita.repository

import com.example.tiendita.dao.UserDao
import com.example.tiendita.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface UserRepository {
    suspend fun insertUser(user: User): Long
    suspend fun getAllUsers(): List<User>
}

class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {

    override suspend fun insertUser(user: User): Long {
        return withContext(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            userDao.getAllUsers()
        }
    }
}
