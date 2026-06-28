package activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import vn.quizmaster.R
import storage.SessionManager
import vn.quizmaster.databinding.ActivityProfileBinding

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val session by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation(R.id.navProfile)
        binding.btnEdit.setOnClickListener { openEditor() }
        binding.rowPersonal.setOnClickListener { openEditor() }
        val messageRows = mapOf(
            binding.rowGoal to "Mục tiêu hiện tại: 20 câu mỗi ngày",
            binding.rowNotification to "Nhắc học lúc 20:00 đang bật",
            binding.rowHistory to "Bạn đã hoàn thành 12 bài thi",
            binding.rowAchievement to "Bạn đã mở khóa 3 thành tích"
        )
        messageRows.forEach { (view, text) ->
            view.setOnClickListener { Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show() }
        }
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất khỏi QuizMaster?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đăng xuất") { _, _ ->
                    session.logout()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        val profile = session.getProfile()
        binding.tvName.text = profile.name
        binding.tvEmail.text = profile.email
        binding.tvAvatar.text = initials(profile.name)
    }

    private fun openEditor() = startActivity(Intent(this, EditProfileActivity::class.java))
}
