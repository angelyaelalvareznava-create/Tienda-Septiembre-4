package com.example.tiendita.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tiendita.model.User


@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long


    @Query("SELECT * FROM users ORDER BY id DESC")
    suspend fun getAllUsers(): List<User>


    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?


    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int
}
