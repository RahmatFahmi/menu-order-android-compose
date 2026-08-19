package com.rahmat.testapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("bearer_token")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val TABLE_CODE = stringPreferencesKey("table_code")
        private val TABLE_ID = stringPreferencesKey("table_id")
        private val ID = stringPreferencesKey("id_user")
    }

    suspend fun saveToken(token: String, role: String, id: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[ROLE_KEY] = role
            preferences[ID] = id
        }
    }

    suspend fun saveTabelCode(tabelCode: String, tableId: String) {
        context.dataStore.edit { preferences ->
            preferences[TABLE_CODE] = tabelCode
            preferences[TABLE_ID] = tableId
        }
    }

    val getId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[ID]
        }

    val getToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    val getTableCode: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TABLE_CODE]
        }
    val getTableId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TABLE_ID]
        }

    val getRole: Flow<String?> = context.dataStore.data
        .map{preferences ->
            preferences[ROLE_KEY]
        }

    suspend fun clearSessionToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(ROLE_KEY)
        }
    }
    suspend fun clearTabelCode() {
        context.dataStore.edit { preferences ->
            preferences.remove(TABLE_CODE)
            preferences.remove(TABLE_ID)
        }
    }
    suspend fun clearAuth() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}