package com.example.tiendita.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.authSessionDataStore by preferencesDataStore(name = "auth_session")

class DataStoreSessionStore(private val dataStore: DataStore<Preferences>) : SessionStore {
    constructor(context: Context) : this(context.applicationContext.authSessionDataStore)

    companion object {
        const val FILE_NAME = "auth_session.preferences_pb"
        private val ACCOUNT_ID = longPreferencesKey("accountId")
    }

    override suspend fun readAccountId(): Long? = dataStore.data.first()[ACCOUNT_ID]

    override suspend fun saveAccountId(accountId: Long) {
        require(accountId > 0)
        dataStore.edit {
            it.clear()
            it[ACCOUNT_ID] = accountId
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
