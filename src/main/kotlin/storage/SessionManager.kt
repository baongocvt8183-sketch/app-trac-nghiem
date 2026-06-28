package storage

import android.content.Context
import models.UserProfile

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("quizmaster_session", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_LOGGED_IN, false)
        private set(value) = prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()

    var darkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    fun login(email: String, remember: Boolean) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putBoolean(KEY_REMEMBER, remember)
            .putString(KEY_EMAIL, email)
            .apply()
        if (getProfile().email == DEFAULT_EMAIL) {
            saveProfile(getProfile().copy(email = email))
        }
        isLoggedIn = true
    }

    fun shouldAutoLogin(): Boolean = isLoggedIn && prefs.getBoolean(KEY_REMEMBER, false)

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).putBoolean(KEY_REMEMBER, false).apply()
    }

    fun getProfile(): UserProfile = UserProfile(
        name = prefs.getString(KEY_NAME, "Khách") ?: "Khách",
        email = prefs.getString(KEY_PROFILE_EMAIL, DEFAULT_EMAIL) ?: DEFAULT_EMAIL,
        phone = prefs.getString(KEY_PHONE, "") ?: "",
        school = prefs.getString(KEY_SCHOOL, "") ?: "",
        grade = prefs.getString(KEY_GRADE, "12") ?: "12"
    )

    fun saveProfile(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_NAME, profile.name)
            .putString(KEY_PROFILE_EMAIL, profile.email)
            .putString(KEY_PHONE, profile.phone)
            .putString(KEY_SCHOOL, profile.school)
            .putString(KEY_GRADE, profile.grade)
            .apply()
    }

    fun clearAll() = prefs.edit().clear().apply()

    companion object {
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_REMEMBER = "remember"
        private const val KEY_EMAIL = "login_email"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NAME = "profile_name"
        private const val KEY_PROFILE_EMAIL = "profile_email"
        private const val KEY_PHONE = "profile_phone"
        private const val KEY_SCHOOL = "profile_school"
        private const val KEY_GRADE = "profile_grade"
        private const val DEFAULT_EMAIL = "guest@quizmaster.vn"
    }
}
