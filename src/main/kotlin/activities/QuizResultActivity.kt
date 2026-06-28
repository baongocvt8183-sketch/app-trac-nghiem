package activities

import android.content.Intent
import android.os.Bundle
import vn.quizmaster.databinding.ActivityQuizResultBinding

class QuizResultActivity : BaseActivity() {
    private lateinit var binding: ActivityQuizResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val total = intent.getIntExtra(EXTRA_TOTAL, 0)
        val percent = if (total == 0) 0 else score * 100 / total
        binding.tvScore.text = "$score / $total"
        binding.tvCorrect.text = "✓  Số câu đúng                                      $score"
        binding.tvWrong.text = "×  Số câu sai                                        ${total - score}"
        binding.tvPercent.text = "◎  Tỷ lệ chính xác                               $percent%"
        binding.tvMessage.text = when {
            percent >= 80 -> "Xuất sắc! Bạn đã nắm rất chắc kiến thức."
            percent >= 50 -> "Hoàn thành tốt! Hãy ôn thêm các câu còn sai."
            else -> "Đừng bỏ cuộc, mỗi lần luyện tập là một bước tiến."
        }
        binding.btnRetry.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java).apply {
                putExtra(QuizActivity.EXTRA_SUBJECT_ID, intent.getLongExtra(QuizActivity.EXTRA_SUBJECT_ID, -1L))
                putExtra(QuizActivity.EXTRA_SUBJECT_NAME, intent.getStringExtra(QuizActivity.EXTRA_SUBJECT_NAME))
            })
            finish()
        }
        binding.btnDone.setOnClickListener {
            startActivity(Intent(this, SubjectsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
            finish()
        }
    }

    companion object {
        const val EXTRA_SCORE = "result_score"
        const val EXTRA_TOTAL = "result_total"
    }
}
