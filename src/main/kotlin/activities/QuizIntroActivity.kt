package activities

import android.content.Intent
import android.os.Bundle
import database.QuizDatabase
import vn.quizmaster.databinding.ActivityQuizIntroBinding

class QuizIntroActivity : BaseActivity() {
    private lateinit var binding: ActivityQuizIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val subjectId = intent.getLongExtra(QuizActivity.EXTRA_SUBJECT_ID, -1L)
        val subjectName = intent.getStringExtra(QuizActivity.EXTRA_SUBJECT_NAME).orEmpty()
        val count = QuizDatabase(applicationContext).use { it.getQuestions(subjectId).size }
        binding.tvTitle.text = intent.getStringExtra(EXTRA_QUIZ_TITLE) ?: "Đề luyện tập"
        binding.tvDifficulty.text = "Mức độ: ${intent.getStringExtra(EXTRA_DIFFICULTY) ?: "Trung bình"}"
        binding.tvQuestionCount.text = "Số câu hỏi                                      $count câu"
        binding.tvDuration.text = "Thời gian làm bài                         ${intent.getIntExtra(EXTRA_DURATION, 15)} phút"
        binding.btnBack.setOnClickListener { finish() }
        binding.btnStart.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java).apply {
                putExtra(QuizActivity.EXTRA_SUBJECT_ID, subjectId)
                putExtra(QuizActivity.EXTRA_SUBJECT_NAME, subjectName)
                putExtra(EXTRA_MODE, binding.modeGroup.checkedRadioButtonId)
            })
        }
    }

    companion object {
        const val EXTRA_QUIZ_TITLE = "quiz_title"
        const val EXTRA_DURATION = "quiz_duration"
        const val EXTRA_DIFFICULTY = "quiz_difficulty"
        const val EXTRA_MODE = "quiz_mode"
    }
}
