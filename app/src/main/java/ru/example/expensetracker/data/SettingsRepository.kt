package ru.example.expensetracker.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Один экземпляр DataStore на файл - объявляется на уровне файла
private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.dataStore
    private val darkKey = booleanPreferencesKey("dark_theme")

    /** null - пользователь ещё не выбирал, следуем системной теме. */
    val darkTheme: Flow<Boolean?> = store.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[darkKey] }

    suspend fun setDarkTheme(enabled: Boolean) {
        store.edit { it[darkKey] = enabled }
    }
}
