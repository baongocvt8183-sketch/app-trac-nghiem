package models

data class Subject(
    val id: Long,
    val name: String,
    val symbol: String,
    val color: String,
    val completed: Int,
    val total: Int,
    val sortOrder: Int,
    val description: String = "",
    val coverImage: String? = null,
    val isPublic: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SubjectInput(
    val name: String,
    val description: String,
    val symbol: String,
    val color: String,
    val coverImage: String? = null,
    val isPublic: Boolean = true
)

data class Question(
    val id: Long,
    val subjectId: Long,
    val content: String,
    val answers: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class QuizSet(
    val id: Int,
    val title: String,
    val questionCount: Int,
    val durationMinutes: Int,
    val difficulty: String,
    val progress: Int,
    val status: String
)

data class UserProfile(
    val name: String = "Khách",
    val email: String = "guest@quizmaster.vn",
    val phone: String = "",
    val school: String = "",
    val grade: String = "12"
)
