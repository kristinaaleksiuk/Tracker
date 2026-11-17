package ru.example.expensetracker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.example.expensetracker.R
import ru.example.expensetracker.data.Expense
import ru.example.expensetracker.databinding.ItemExpenseBinding
import ru.example.expensetracker.util.Dates
import ru.example.expensetracker.util.Money

class ExpenseAdapter(
    private val onLongClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.VH>(Diff) {

    class VH(val b: ItemExpenseBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = getItem(position)
        val ctx = holder.itemView.context
        val names = ctx.resources.getStringArray(R.array.categories)
        holder.b.tvCategory.text = names[e.categoryId.coerceIn(names.indices)]
        holder.b.tvComment.text = e.comment.ifBlank { ctx.getString(R.string.no_comment) }
        holder.b.tvDate.text = Dates.format(e.date)
        holder.b.tvAmount.text = ctx.getString(R.string.amount_rub, Money.format(e.amount))
        holder.b.root.setOnLongClickListener { onLongClick(e); true }
    }

    private object Diff : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(a: Expense, b: Expense) = a.id == b.id
        override fun areContentsTheSame(a: Expense, b: Expense) = a == b
    }
}
