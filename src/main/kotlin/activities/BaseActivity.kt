package activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import utils.toInitials
import vn.quizmaster.R

abstract class BaseActivity : AppCompatActivity() {
    protected fun setupBottomNavigation(activeId: Int) {
        val destinations = mapOf(
            R.id.navHome to HomeActivity::class.java,
            R.id.navSubjects to SubjectsActivity::class.java,
            R.id.navProfile to ProfileActivity::class.java,
            R.id.navMore to MoreActivity::class.java
        )
        destinations.forEach { (id, target) ->
            findViewById<View?>(id)?.apply {
                isSelected = id == activeId
                alpha = if (id == activeId) 1f else 0.65f
                setOnClickListener {
                    if (id != activeId) {
                        startActivity(Intent(this@BaseActivity, target).apply {
                            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        })
                        overridePendingTransition(0, 0)
                    }
                }
            }
        }
        findViewById<View?>(R.id.navStats)?.setOnClickListener {
            com.google.android.material.snackbar.Snackbar
                .make(findViewById(android.R.id.content), "Thống kê đang được tổng hợp", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    protected fun initials(name: String): String =
        name.toInitials()

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("saved_at", System.currentTimeMillis())
        super.onSaveInstanceState(outState)
    }
}
