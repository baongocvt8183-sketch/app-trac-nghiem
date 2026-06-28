package adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import models.QuizSet
import vn.quizmaster.databinding.ItemQuizSetBinding

class QuizSetAdapter(
    private val items: List<QuizSet>,
    private val onClick: (QuizSet) -> Unit
) : RecyclerView.Adapter<QuizSetAdapter.QuizViewHolder>() {

    class QuizViewHolder(val binding: ItemQuizSetBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder =
        QuizViewHolder(ItemQuizSetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvTitle.text = item.title
            tvMeta.text = "${item.questionCount} câu • ${item.durationMinutes} phút • ${item.difficulty}"
            progress.progress = item.progress
            btnAction.text = if (item.progress > 0) "Làm tiếp" else "Bắt đầu"
            btnAction.setOnClickListener { onClick(item) }
            root.setOnClickListener { onClick(item) }
        }
    }

    override fun getItemCount(): Int = items.size
}
