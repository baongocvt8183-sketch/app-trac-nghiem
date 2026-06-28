package adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import models.Subject
import vn.quizmaster.databinding.ItemQuickSubjectBinding

class QuickSubjectAdapter(
    private val items: List<Subject>,
    private val onClick: (Subject) -> Unit
) : RecyclerView.Adapter<QuickSubjectAdapter.QuickViewHolder>() {

    class QuickViewHolder(val binding: ItemQuickSubjectBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickViewHolder =
        QuickViewHolder(
            ItemQuickSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvIcon.text = item.symbol
            tvName.text = item.name
            tvCount.text = "${item.total} câu"
            tvIcon.background = GradientDrawable().apply {
                cornerRadius = 20f
                setColor(Color.parseColor(item.color))
            }
            root.setOnClickListener { onClick(item) }
        }
    }

    override fun getItemCount(): Int = items.size
}
