package vn.quizmaster

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import storage.SessionManager

class QuizApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val mode = if (SessionManager(this).darkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
