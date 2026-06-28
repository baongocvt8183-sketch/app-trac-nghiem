package activities

import android.os.Bundle
import android.util.Patterns
import com.google.android.material.snackbar.Snackbar
import models.UserProfile
import storage.SessionManager
import vn.quizmaster.databinding.ActivityEditProfileBinding

class EditProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val session by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val profile = session.getProfile()
        binding.edtName.setText(savedInstanceState?.getString("name") ?: profile.name)
        binding.edtEmail.setText(savedInstanceState?.getString("email") ?: profile.email)
        binding.edtPhone.setText(savedInstanceState?.getString("phone") ?: profile.phone)
        binding.edtSchool.setText(savedInstanceState?.getString("school") ?: profile.school)
        binding.edtGrade.setText(savedInstanceState?.getString("grade") ?: profile.grade)
        binding.tvAvatar.text = initials(profile.name)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveProfile() }
    }

    private fun saveProfile() {
        val profile = UserProfile(
            name = binding.edtName.text.toString().trim(),
            email = binding.edtEmail.text.toString().trim(),
            phone = binding.edtPhone.text.toString().trim(),
            school = binding.edtSchool.text.toString().trim(),
            grade = binding.edtGrade.text.toString().trim()
        )
        clearErrors()
        when {
            profile.name.isBlank() -> binding.edtName.error = "Họ tên không được để trống"
            profile.name.length < 2 -> binding.edtName.error = "Họ tên quá ngắn"
            profile.email.isBlank() -> binding.edtEmail.error = "Email không được để trống"
            !Patterns.EMAIL_ADDRESS.matcher(profile.email).matches() -> binding.edtEmail.error = "Email không đúng định dạng"
            profile.phone.isNotBlank() && !profile.phone.matches(Regex("^(\\+84|0)\\d{9}$")) ->
                binding.edtPhone.error = "Số điện thoại phải có 10 chữ số"
            profile.school.isBlank() -> binding.edtSchool.error = "Tên trường không được để trống"
            profile.grade.isBlank() -> binding.edtGrade.error = "Lớp/khối không được để trống"
            else -> {
                binding.btnSave.isEnabled = false
                session.saveProfile(profile)
                binding.tvAvatar.text = initials(profile.name)
                Snackbar.make(binding.root, "Đã lưu thay đổi", Snackbar.LENGTH_SHORT).show()
                binding.root.postDelayed({ finish() }, 500)
            }
        }
    }

    private fun clearErrors() {
        listOf(binding.edtName, binding.edtEmail, binding.edtPhone, binding.edtSchool, binding.edtGrade)
            .forEach { it.error = null }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("name", binding.edtName.text.toString())
        outState.putString("email", binding.edtEmail.text.toString())
        outState.putString("phone", binding.edtPhone.text.toString())
        outState.putString("school", binding.edtSchool.text.toString())
        outState.putString("grade", binding.edtGrade.text.toString())
        super.onSaveInstanceState(outState)
    }
}
