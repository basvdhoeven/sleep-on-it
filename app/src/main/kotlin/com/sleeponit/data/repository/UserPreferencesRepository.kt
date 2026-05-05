package com.sleeponit.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")
private val CURRENCY_CODE_KEY = stringPreferencesKey("currency_code")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val currencyCode: Flow<String> = context.userPreferencesDataStore.data
        .map { it[CURRENCY_CODE_KEY] ?: "USD" }

    suspend fun setCurrencyCode(code: String) {
        context.userPreferencesDataStore.edit { it[CURRENCY_CODE_KEY] = code }
    }
}
