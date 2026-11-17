package ru.example.expensetracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ru.example.expensetracker.data.AppDatabase
import ru.example.expensetracker.data.ExpenseRepository
import ru.example.expensetracker.data.SettingsRepository

class ExpenseApp : Application() {
    val database by lazy { AppDatabase.get(this) }
    val repository by lazy { ExpenseRepository(database.expenseDao()) }
    val settings by lazy { SettingsRepository(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    override fun onCreate() {
        super.onCreate()
        // Единственное место, где тема применяется: любое изменение настройки
        // (и значение при запуске) автоматически переключает режим.
        appScope.launch {
            settings.darkTheme.distinctUntilChanged().collect { dark ->
                AppCompatDelegate.setDefaultNightMode(
                    when (dark) {
                        true -> AppCompatDelegate.MODE_NIGHT_YES
                        false -> AppCompatDelegate.MODE_NIGHT_NO
                        null -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                )
            }
        }
    }
}
