package activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import storage.SessionManager
import vn.quizmaster.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)
        if (session.shouldAutoLogin()) {
            openHome()
            return
        }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLogin.setOnClickListener { submitLogin() }
        binding.tvGuest.setOnClickListener {
            session.login("guest@quizmaster.vn", false)
            openHome()
        }
    }

    private fun submitLogin() {
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString()
        binding.edtEmail.error = null
        binding.edtPassword.error = null
        when {
            email.isBlank() -> binding.edtEmail.error = "Email không được để trống"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> binding.edtEmail.error = "Email không đúng định dạng"
            password.isBlank() -> binding.edtPassword.error = "Mật khẩu không được để trống"
            password.length < 6 -> binding.edtPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
            else -> {
                binding.btnLogin.isEnabled = false
                session.login(email, binding.checkRemember.isChecked)
                Snackbar.make(binding.root, "Đăng nhập thành công", Snackbar.LENGTH_SHORT).show()
                binding.root.postDelayed(::openHome, 350)
            }
        }
    }

    private fun openHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
