package ru.example.expensetracker.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import ru.example.expensetracker.R
import ru.example.expensetracker.databinding.ActivityCategoryBinding

/** Возвращает выбранную категорию через Activity Result API. */
class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val names = resources.getStringArray(R.array.categories)
        binding.list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, names)
        val current = intent.getIntExtra(EXTRA_ID, 0)
        binding.list.setItemChecked(current, true)

        binding.list.setOnItemClickListener { _, _, position, _ ->
            setResult(RESULT_OK, Intent().putExtra(EXTRA_ID, position))
            finish()
        }
    }

    companion object {
        const val EXTRA_ID = "extra_category_id"
    }
}
