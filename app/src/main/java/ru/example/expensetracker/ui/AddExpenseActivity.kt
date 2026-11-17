package ru.example.expensetracker.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.MaterialColors
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import ru.example.expensetracker.R
import ru.example.expensetracker.databinding.ActivityAddExpenseBinding
import ru.example.expensetracker.util.Dates
import ru.example.expensetracker.util.Money
import ru.example.expensetracker.util.MoneyMaskWatcher

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private val viewModel: AddExpenseViewModel by viewModels()
    private var amountTouched = false

    // Регистрируем при инициализации Activity, а не в обработчике клика
    private val pickCategory = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val id = result.data?.getIntExtra(CategoryActivity.EXTRA_ID, -1) ?: -1
            if (id >= 0) {
                viewModel.categoryId = id
                renderCategory()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        renderCategory()
        renderDate()

        // TextWatcher №1: форматирование суммы
        binding.amountInput.addTextChangedListener(MoneyMaskWatcher(binding.amountInput))
        // TextWatcher №2: проверка суммы > 0 в реальном времени
        binding.amountInput.doAfterTextChanged {
            if (binding.amountInput.hasFocus()) amountTouched = true
            validate(showAmountError = amountTouched)
        }
        // TextWatcher №3: счётчик символов комментария
        binding.commentInput.doAfterTextChanged { text ->
            val length = text?.length ?: 0
            binding.commentCounter.text = getString(R.string.counter, length, COMMENT_LIMIT)
            val attr = if (length > COMMENT_LIMIT) com.google.android.material.R.attr.colorError
            else com.google.android.material.R.attr.colorOnSurfaceVariant
            binding.commentCounter.setTextColor(MaterialColors.getColor(binding.commentCounter, attr))
            validate(showAmountError = amountTouched)
        }

        binding.categoryButton.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
                .putExtra(CategoryActivity.EXTRA_ID, viewModel.categoryId)
            try {
                pickCategory.launch(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show()
            }
        }
        binding.dateInput.setOnClickListener { showDatePicker() }
        binding.saveButton.setOnClickListener { save() }

        validate(showAmountError = false)
    }

    private fun renderCategory() {
        val names = resources.getStringArray(R.array.categories)
        binding.categoryButton.text = getString(
            R.string.category_value, names[viewModel.categoryId.coerceIn(names.indices)]
        )
    }

    private fun renderDate() {
        binding.dateInput.setText(Dates.format(viewModel.dateMillis))
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(Dates.toPicker(viewModel.dateMillis))
            .build()
        picker.addOnPositiveButtonClickListener {
            viewModel.dateMillis = Dates.fromPicker(it)
            renderDate()
        }
        picker.show(supportFragmentManager, "date")
    }

    /** Возвращает true, если форму можно сохранять. Кнопка активна только при корректных данных. */
    private fun validate(showAmountError: Boolean): Boolean {
        val cents = Money.parse(binding.amountInput.text.toString())
        val commentOk = (binding.commentInput.text?.length ?: 0) <= COMMENT_LIMIT
        val amountOk = cents > 0
        binding.amountLayout.error =
            if (!amountOk && showAmountError) getString(R.string.error_amount) else null
        val ok = amountOk && commentOk
        binding.saveButton.isEnabled = ok
        return ok
    }

    private fun save() {
        if (!validate(showAmountError = true)) return
        val cents = Money.parse(binding.amountInput.text.toString())
        val comment = binding.commentInput.text.toString().trim()
        lifecycleScope.launch {
            viewModel.save(cents, comment)
            finish()
        }
    }

    companion object {
        private const val COMMENT_LIMIT = 100
    }
}
