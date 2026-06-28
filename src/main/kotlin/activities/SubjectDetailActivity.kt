package activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import adapters.QuizSetAdapter
import com.google.android.material.snackbar.Snackbar
import database.QuizDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.QuizSet
import models.Subject
import vn.quizmaster.R
import vn.quizmaster.databinding.ActivitySubjectDetailBinding

class SubjectDetailActivity : BaseActivity() {
    private lateinit var binding: ActivitySubjectDetailBinding
    private val database by lazy { QuizDatabase(applicationContext) }
    private var subject: Subject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation(R.id.navSubjects)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnMenu.setOnClickListener(::showMenu)
        setupTabs()
        lifecycleScope.launch {
            val id = intent.getLongExtra(EXTRA_SUBJECT_ID, -1L)
            subject = withContext(Dispatchers.IO) { database.getSubject(id) }
            subject?.let(::renderSubject) ?: finish()
        }
    }

    private fun renderSubject(item: Subject) {
        val percent = if (item.total == 0) 0 else item.completed * 100 / item.total
        binding.tvToolbarTitle.text = item.name
        binding.tvName.text = item.name
        binding.tvDescription.text = item.description.ifBlank { "Khám phá kiến thức và luyện tập theo chủ đề" }
        binding.tvIcon.text = item.symbol
        binding.tvIcon.background = GradientDrawable().apply {
            cornerRadius = 22f
            setColor(Color.parseColor(item.color))
        }
        binding.tvStats.text = "${item.total} câu • $percent% hoàn thành"
        binding.progressSubject.progress = percent
        val quizSets = listOf(
            QuizSet(1, "Đề 1: ${item.name} cơ bản", item.total.coerceAtLeast(10), 15, "Dễ", percent, "Đang học"),
            QuizSet(2, "Đề 2: Luyện tập tổng hợp", item.total.coerceAtLeast(10), 20, "Trung bình", 0, "Chưa làm"),
            QuizSet(3, "Đề 3: Thử thách nâng cao", item.total.coerceAtLeast(10), 25, "Khó", 0, "Chưa làm")
        )
        binding.rvQuizSets.layoutManager = LinearLayoutManager(this)
        binding.rvQuizSets.adapter = QuizSetAdapter(quizSets) { openIntro(it) }
        binding.actionContinue.setOnClickListener { openIntro(quizSets.first()) }
        binding.actionRandom.setOnClickListener { openQuiz() }
        binding.actionDocument.setOnClickListener { showFeature("Tài liệu môn học", "Chưa có tài liệu. Bạn có thể thêm ở phiên bản tiếp theo.") }
        binding.actionStats.setOnClickListener { selectTab(binding.tabStatistics) }
    }

    private fun setupTabs() {
        val tabs = listOf(
            binding.tabQuizzes to "Đề thi",
            binding.tabStudy to "Flashcard, ghi chú, câu sai và câu đã lưu sẽ xuất hiện tại đây.",
            binding.tabDocuments to "Tài liệu PDF, DOCX, video và audio của môn học.",
            binding.tabStatistics to "Dashboard tiến độ, tỷ lệ đúng và thời gian học.",
            binding.tabDiscussion to "Khu vực hỏi đáp và thảo luận cùng người học khác."
        )
        tabs.forEach { (tab, _) -> tab.setOnClickListener { selectTab(tab) } }
        selectTab(binding.tabQuizzes)
    }

    private fun selectTab(selected: View) {
        val tabs = listOf(
            binding.tabQuizzes,
            binding.tabStudy,
            binding.tabDocuments,
            binding.tabStatistics,
            binding.tabDiscussion
        )
        tabs.forEach {
            it.isSelected = it == selected
            it.setTextColor(if (it == selected) Color.WHITE else getColor(R.color.text_primary))
        }
        val quizzesSelected = selected == binding.tabQuizzes
        binding.rvQuizSets.visibility = if (quizzesSelected) View.VISIBLE else View.GONE
        binding.tvTabPlaceholder.visibility = if (quizzesSelected) View.GONE else View.VISIBLE
        binding.tvTabPlaceholder.text = when (selected) {
            binding.tabStudy -> "Ôn tập\nFlashcard • Ghi chú • Câu sai • Câu đã lưu"
            binding.tabDocuments -> "Tài liệu môn học\nPDF • DOCX • Video • Audio"
            binding.tabStatistics -> "Thống kê tiến độ\nTỷ lệ đúng • Thời gian học • Điểm số"
            else -> "Thảo luận\nĐặt câu hỏi và chia sẻ kiến thức"
        }
    }

    private fun showMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            menu.add("Chỉnh sửa")
            menu.add("Chia sẻ")
            menu.add("Lưu trữ")
            menu.add("Xóa môn học")
            setOnMenuItemClickListener { item ->
                when (item.title.toString()) {
                    "Chia sẻ" -> shareSubject()
                    "Xóa môn học" -> confirmDelete()
                    else -> Snackbar.make(binding.root, "${item.title}: đang được phát triển", Snackbar.LENGTH_SHORT).show()
                }
                true
            }
            show()
        }
    }

    private fun shareSubject() {
        val item = subject ?: return
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Cùng học ${item.name} trên QuizMaster!")
        }, "Chia sẻ môn học"))
    }

    private fun confirmDelete() {
        val item = subject ?: return
        AlertDialog.Builder(this)
            .setTitle("Xóa ${item.name}?")
            .setMessage("Câu hỏi và tiến độ của môn học cũng sẽ bị xóa.")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    database.deleteSubject(item.id)
                    withContext(Dispatchers.Main) { finish() }
                }
            }.show()
    }

    private fun openIntro(quizSet: QuizSet) {
        val item = subject ?: return
        startActivity(Intent(this, QuizIntroActivity::class.java).apply {
            putExtra(QuizActivity.EXTRA_SUBJECT_ID, item.id)
            putExtra(QuizActivity.EXTRA_SUBJECT_NAME, item.name)
            putExtra(QuizIntroActivity.EXTRA_QUIZ_TITLE, quizSet.title)
            putExtra(QuizIntroActivity.EXTRA_DURATION, quizSet.durationMinutes)
            putExtra(QuizIntroActivity.EXTRA_DIFFICULTY, quizSet.difficulty)
        })
    }

    private fun openQuiz() {
        val item = subject ?: return
        startActivity(Intent(this, QuizActivity::class.java).apply {
            putExtra(QuizActivity.EXTRA_SUBJECT_ID, item.id)
            putExtra(QuizActivity.EXTRA_SUBJECT_NAME, item.name)
        })
    }

    private fun showFeature(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("Đóng", null).show()
    }

    companion object {
        const val EXTRA_SUBJECT_ID = "detail_subject_id"
    }
}
