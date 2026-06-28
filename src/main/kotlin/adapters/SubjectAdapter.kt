package adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.quizmaster.databinding.ItemSubjectBinding
import models.Subject

class SubjectAdapter(
    private val items: MutableList<Subject>,
    private val listener: Listener
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    interface Listener {
        fun onSubjectClick(subject: Subject)
        fun onSubjectLongClick(subject: Subject)
        fun onDragRequested(holder: RecyclerView.ViewHolder)
    }

    inner class SubjectViewHolder(val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder =
        SubjectViewHolder(ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvName.text = item.name
            tvIcon.text = item.symbol
            tvDescription.text = item.description.ifBlank { "Chạm để xem chi tiết và luyện tập" }
            val percent = if (item.total == 0) 0 else item.completed * 100 / item.total
            tvProgress.text = "${item.total} câu • $percent% hoàn thành"
            progressBar.progress = percent
            tvIcon.background = coloredBackground(item.color)
            progressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(item.color))
            itemRoot.setOnClickListener { listener.onSubjectClick(item) }
            itemRoot.setOnLongClickListener {
                listener.onSubjectLongClick(item)
                true
            }
            tvDrag.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) listener.onDragRequested(holder)
                false
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun currentItems(): List<Subject> = items.toList()

    fun submitItems(newItems: List<Subject>, highlightId: Long? = null) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
        val index = items.indexOfFirst { it.id == highlightId }
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    fun remove(subject: Subject) {
        val index = items.indexOfFirst { it.id == subject.id }
        if (index >= 0) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun move(from: Int, to: Int): Boolean {
        if (from !in items.indices || to !in items.indices) return false
        val item = items.removeAt(from)
        items.add(to, item)
        notifyItemMoved(from, to)
        return true
    }

    private fun coloredBackground(color: String) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 20f
        setColor(Color.parseColor(color))
    }
}
