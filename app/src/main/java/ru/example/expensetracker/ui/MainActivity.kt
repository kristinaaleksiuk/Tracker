package ru.example.expensetracker.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import ru.example.expensetracker.ExpenseApp
import ru.example.expensetracker.R
import ru.example.expensetracker.data.Expense
import ru.example.expensetracker.databinding.ActivityMainBinding
import ru.example.expensetracker.util.Money

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val adapter = ExpenseAdapter { confirmDelete(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.expenses.collect {
                        adapter.submitList(it)
                        binding.emptyText.isVisible = it.isEmpty()
                    }
                }
                launch {
                    viewModel.monthTotal.collect {
                        binding.totalValue.text = getString(R.string.amount_rub, Money.format(it))
                    }
                }
            }
        }

        binding.addButton.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java)) // явный Intent
        }
        binding.statsButton.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        // Состояние переключателя берём из реально действующей темы, затем вешаем слушатель
        val nightNow = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        binding.themeSwitch.isChecked = nightNow
        binding.themeSwitch.setOnCheckedChangeListener { _, checked ->
            // Сохраняем в DataStore; ExpenseApp сам применит тему
            lifecycleScope.launch { (application as ExpenseApp).settings.setDarkTheme(checked) }
        }
    }

    private fun confirmDelete(expense: Expense) {
        MaterialAlertDialogBuilder(this) // диалогу нужен именно Activity Context
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_message)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.delete(expense) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
