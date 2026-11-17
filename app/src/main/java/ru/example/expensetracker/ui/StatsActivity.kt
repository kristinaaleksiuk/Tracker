package ru.example.expensetracker.ui

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.example.expensetracker.R
import ru.example.expensetracker.data.CategoryTotal
import ru.example.expensetracker.databinding.ActivityStatsBinding
import ru.example.expensetracker.databinding.ItemStatBinding
import ru.example.expensetracker.util.Dates
import ru.example.expensetracker.util.Money

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private val viewModel: StatsViewModel by viewModels()

    // Storage Access Framework: пользователь сам выбирает, куда сохранить файл, разрешения не нужны
    private val createDoc = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri -> uri?.let { export(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.byCategory.collect { render(it) }
            }
        }

        binding.exportButton.setOnClickListener {
            try {
                createDoc.launch("expenses_${Dates.formatMonth(System.currentTimeMillis()).replace('.', '_')}.txt")
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, R.string.error_no_app, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun render(items: List<CategoryTotal>) {
        val names = resources.getStringArray(R.array.categories)
        val sum = items.sumOf { it.total }
        binding.totalValue.text = getString(R.string.amount_rub, Money.format(sum))
        binding.emptyText.isVisible = items.isEmpty()
        binding.container.removeAllViews()
        items.forEach { item ->
            val row = ItemStatBinding.inflate(layoutInflater, binding.container, false)
            row.tvName.text = names[item.categoryId.coerceIn(names.indices)]
            row.tvAmount.text = getString(R.string.amount_rub, Money.format(item.total))
            row.progress.setProgressCompat(if (sum > 0) (item.total * 100 / sum).toInt() else 0, false)
            binding.container.addView(row.root)
        }
    }

    private fun export(uri: android.net.Uri) {
        lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) { // запись файла - не в главном потоке
                runCatching {
                    val text = viewModel.buildReport()
                    contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
                        ?: error("no stream")
                }.isSuccess
            }
            Toast.makeText(
                this@StatsActivity,
                if (ok) R.string.export_ok else R.string.error_generic,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
