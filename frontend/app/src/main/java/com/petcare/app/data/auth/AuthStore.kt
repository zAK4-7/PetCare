package com.petcare.app.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("auth")

class AuthStore(private val context: Context) {
    private val KEY_TOKEN = stringPreferencesKey("token")

    val tokenFlow: Flow<String?> = context.ds.data.map { it[KEY_TOKEN] }

    suspend fun saveToken(token: String) {
        context.ds.edit { it[KEY_TOKEN] = token }
    }

    suspend fun clear() {
        context.ds.edit { it.remove(KEY_TOKEN) }
    }
}
