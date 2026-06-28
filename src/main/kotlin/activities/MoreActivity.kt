package activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import database.QuizDatabase
import storage.SessionManager
import vn.quizmaster.R
import vn.quizmaster.databinding.ActivityMoreBinding

class MoreActivity : BaseActivity() {
    private lateinit var binding: ActivityMoreBinding
    private val session by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation(R.id.navMore)
        binding.switchDark.isChecked = session.darkMode
        updateDarkLabel()
        binding.switchDark.setOnCheckedChangeListener { _, checked ->
            session.darkMode = checked
            updateDarkLabel()
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        binding.rowDark.setOnClickListener { binding.switchDark.toggle() }
        binding.rowPrivacy.setOnClickListener { showInfo("Quyền riêng tư", "Dữ liệu học tập chỉ được lưu cục bộ trên thiết bị bằng SQLite. Ứng dụng không yêu cầu quyền nhạy cảm.") }
        binding.rowAbout.setOnClickListener { showInfo("QuizMaster", "Phiên bản 1.0.0\nỨng dụng luyện trắc nghiệm bằng Kotlin.") }
        binding.rowHelp.setOnClickListener { showInfo("Trợ giúp", "Chọn môn học để bắt đầu. Nhấn giữ môn để xóa; giữ dấu › và kéo để sắp xếp.") }
        binding.rowDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Xóa dữ liệu học tập?")
                .setMessage("Tiến độ và thứ tự môn học sẽ trở về mặc định. Hồ sơ đăng nhập vẫn được giữ.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa") { _, _ ->
                    QuizDatabase(this).use { it.resetLearningData() }
                    Snackbar.make(binding.root, "Đã đặt lại dữ liệu học tập", Snackbar.LENGTH_LONG).show()
                }.show()
        }
    }

    private fun updateDarkLabel() {
        binding.tvDark.text = "Chế độ tối\n${if (binding.switchDark.isChecked) "Đang bật" else "Đang tắt"}"
    }

    private fun showInfo(title: String, message: String) =
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("Đóng", null).show()
}
