package activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import database.QuizDatabase
import models.Question
import vn.quizmaster.databinding.ActivityQuizBinding

class QuizActivity : BaseActivity() {
    private lateinit var binding: ActivityQuizBinding
    private val db by lazy { QuizDatabase(this) }
    private var questions: List<Question> = emptyList()
    private var index = 0
    private var score = 0
    private var checked = false
    private var selectedAnswer = -1
    private var subjectId = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subjectId = intent.getLongExtra(EXTRA_SUBJECT_ID, -1L)
        questions = db.getQuestions(subjectId)
        index = savedInstanceState?.getInt(KEY_INDEX) ?: 0
        score = savedInstanceState?.getInt(KEY_SCORE) ?: 0
        checked = savedInstanceState?.getBoolean(KEY_CHECKED) ?: false
        selectedAnswer = savedInstanceState?.getInt(KEY_SELECTED, -1) ?: -1
        binding.tvSubject.text = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: "Bài luyện tập"
        binding.btnClose.setOnClickListener { confirmExit() }
        binding.btnNext.setOnClickListener { handleNext() }
        if (questions.isEmpty()) {
            AlertDialog.Builder(this).setTitle("Không có câu hỏi").setMessage("Môn học này chưa có dữ liệu.")
                .setPositiveButton("Quay lại") { _, _ -> finish() }.setCancelable(false).show()
        } else {
            index = index.coerceIn(0, questions.lastIndex)
            renderQuestion(restoreState = savedInstanceState != null)
        }
    }

    private fun renderQuestion(restoreState: Boolean = false) {
        val restoredChecked = checked
        val restoredAnswer = selectedAnswer
        val question = questions[index]
        binding.tvCounter.text = "CÂU ${index + 1} / ${questions.size}"
        binding.tvQuestion.text = question.content
        binding.tvScore.text = score.toString()
        binding.progressQuiz.progress = (index + 1) * 100 / questions.size
        listOf(binding.answerA, binding.answerB, binding.answerC, binding.answerD)
            .forEachIndexed { answerIndex, radio -> radio.text = question.answers[answerIndex] }
        binding.answerGroup.clearCheck()
        binding.answerGroup.isEnabled = true
        binding.btnNext.text = "Kiểm tra"
        checked = false
        selectedAnswer = -1
        if (restoreState && restoredAnswer in 0..3) {
            val answers = listOf(binding.answerA, binding.answerB, binding.answerC, binding.answerD)
            answers[restoredAnswer].isChecked = true
            selectedAnswer = restoredAnswer
            checked = restoredChecked
            if (checked) {
                binding.answerGroup.isEnabled = false
                binding.btnNext.text = if (index == questions.lastIndex) "Xem kết quả" else "Câu tiếp theo"
            }
        }
    }

    private fun handleNext() {
        if (!checked) {
            val checkedId = binding.answerGroup.checkedRadioButtonId
            if (checkedId == -1) {
                Snackbar.make(binding.root, "Vui lòng chọn một đáp án", Snackbar.LENGTH_SHORT).show()
                return
            }
            val selected = listOf(binding.answerA, binding.answerB, binding.answerC, binding.answerD)
                .indexOfFirst { it.id == checkedId }
            selectedAnswer = selected
            val question = questions[index]
            if (selected == question.correctIndex) {
                score++
                Snackbar.make(binding.root, "Chính xác! ${question.explanation}", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(binding.root, "Chưa đúng. ${question.explanation}", Snackbar.LENGTH_LONG).show()
            }
            checked = true
            binding.answerGroup.isEnabled = false
            binding.btnNext.text = if (index == questions.lastIndex) "Xem kết quả" else "Câu tiếp theo"
        } else if (index < questions.lastIndex) {
            index++
            renderQuestion()
        } else {
            db.updateProgress(subjectId, score)
            startActivity(Intent(this, QuizResultActivity::class.java).apply {
                putExtra(QuizResultActivity.EXTRA_SCORE, score)
                putExtra(QuizResultActivity.EXTRA_TOTAL, questions.size)
                putExtra(EXTRA_SUBJECT_ID, subjectId)
                putExtra(EXTRA_SUBJECT_NAME, binding.tvSubject.text.toString())
            })
            finish()
        }
    }

    private fun confirmExit() {
        AlertDialog.Builder(this).setTitle("Thoát bài làm?")
            .setMessage("Tiến độ bài hiện tại sẽ không được lưu.")
            .setNegativeButton("Tiếp tục làm", null)
            .setPositiveButton("Thoát") { _, _ -> finish() }.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_INDEX, index)
        outState.putInt(KEY_SCORE, score)
        outState.putBoolean(KEY_CHECKED, checked)
        outState.putInt(KEY_SELECTED, selectedAnswer)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() { db.close(); super.onDestroy() }

    companion object {
        const val EXTRA_SUBJECT_ID = "subject_id"
        const val EXTRA_SUBJECT_NAME = "subject_name"
        private const val KEY_INDEX = "quiz_index"
        private const val KEY_SCORE = "quiz_score"
        private const val KEY_CHECKED = "quiz_checked"
        private const val KEY_SELECTED = "quiz_selected"
    }
}
