package activities

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import adapters.QuickSubjectAdapter
import database.QuizDatabase
import models.Subject
import storage.SessionManager
import vn.quizmaster.R
import vn.quizmaster.databinding.ActivityHomeBinding

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val db by lazy { QuizDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation(R.id.navHome)
        binding.rvQuickSubjects.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.btnChallenge.setOnClickListener {
            db.getSubjects().firstOrNull()?.let(::openQuiz)
        }
    }

    override fun onResume() {
        super.onResume()
        val profile = SessionManager(this).getProfile()
        binding.tvWelcome.text = "Mừng trở lại, ${profile.name}"
        binding.tvName.text = profile.name
        binding.tvInitials.text = initials(profile.name)
        binding.rvQuickSubjects.adapter = QuickSubjectAdapter(db.getSubjects().take(4), ::openQuiz)
    }

    private fun openQuiz(subject: Subject) {
        startActivity(Intent(this, QuizActivity::class.java).apply {
            putExtra(QuizActivity.EXTRA_SUBJECT_ID, subject.id)
            putExtra(QuizActivity.EXTRA_SUBJECT_NAME, subject.name)
        })
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}
