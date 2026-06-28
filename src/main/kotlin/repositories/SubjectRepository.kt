package repositories

import database.QuizDatabase
import models.Subject
import models.SubjectInput

class SubjectRepository(private val database: QuizDatabase) {

    fun getSubjects(): List<Subject> = database.getSubjects()

    fun getSubject(id: Long): Subject? = database.getSubject(id)

    fun addSubject(input: SubjectInput): Result<Subject> {
        val name = input.name.trim()
        return runCatching {
            require(name.length in 3..50) { "Tên môn học phải từ 3 đến 50 ký tự" }
            require(input.description.length <= 300) { "Mô tả tối đa 300 ký tự" }
            require(!database.subjectNameExists(name)) { "Tên môn học đã tồn tại" }
            database.addSubject(input.copy(name = name))
        }
    }

    fun deleteSubject(id: Long): Boolean = database.deleteSubject(id)

    fun saveOrder(subjects: List<Subject>) = database.saveSubjectOrder(subjects)
}
