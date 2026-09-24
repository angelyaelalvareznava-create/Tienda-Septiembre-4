package com.example.tiendita.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tiendita.data.local.entity.UserAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert
    suspend fun insertAccount(account: UserAccountEntity): Long

    @Update
    suspend fun updateAccount(account: UserAccountEntity)

    @Query("SELECT * FROM user_accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE username = :username")
    suspend fun getAccountByUsername(username: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts")
    fun getAllAccountsFlow(): Flow<List<UserAccountEntity>>
}
