package database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import models.Question
import models.Subject
import models.SubjectInput

class QuizDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE subjects(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                symbol TEXT NOT NULL,
                color TEXT NOT NULL,
                completed INTEGER NOT NULL DEFAULT 0,
                total INTEGER NOT NULL DEFAULT 10,
                sort_order INTEGER NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                cover_image TEXT,
                is_public INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL DEFAULT 0,
                updated_at INTEGER NOT NULL DEFAULT 0
            )"""
        )
        db.execSQL(
            """CREATE TABLE questions(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                subject_id INTEGER NOT NULL,
                content TEXT NOT NULL,
                answer_a TEXT NOT NULL,
                answer_b TEXT NOT NULL,
                answer_c TEXT NOT NULL,
                answer_d TEXT NOT NULL,
                correct_index INTEGER NOT NULL,
                explanation TEXT NOT NULL,
                FOREIGN KEY(subject_id) REFERENCES subjects(id) ON DELETE CASCADE
            )"""
        )
        seed(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE subjects ADD COLUMN description TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE subjects ADD COLUMN cover_image TEXT")
            db.execSQL("ALTER TABLE subjects ADD COLUMN is_public INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE subjects ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE subjects ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE subjects SET description='Đại số, hình học và luyện thi' WHERE name='Toán học'")
            db.execSQL("UPDATE subjects SET description='Cơ học, điện học và quang học' WHERE name='Vật lý'")
            db.execSQL("UPDATE subjects SET description='Phản ứng và công thức hóa học' WHERE name='Hóa học'")
            db.execSQL("UPDATE subjects SET description='Từ vựng, ngữ pháp và đọc hiểu' WHERE name='Tiếng Anh'")
            db.execSQL("UPDATE subjects SET description='Tác phẩm và nghị luận văn học' WHERE name='Văn học'")
            db.execSQL("UPDATE subjects SET description='Sự kiện và các mốc lịch sử' WHERE name='Lịch sử'")
        }
    }

    fun getSubjects(): MutableList<Subject> {
        val result = mutableListOf<Subject>()
        readableDatabase.query(
            "subjects", null, null, null, null, null, "sort_order ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += Subject(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    symbol = cursor.getString(cursor.getColumnIndexOrThrow("symbol")),
                    color = cursor.getString(cursor.getColumnIndexOrThrow("color")),
                    completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed")),
                    total = cursor.getInt(cursor.getColumnIndexOrThrow("total")),
                    sortOrder = cursor.getInt(cursor.getColumnIndexOrThrow("sort_order")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    coverImage = cursor.getString(cursor.getColumnIndexOrThrow("cover_image")),
                    isPublic = cursor.getInt(cursor.getColumnIndexOrThrow("is_public")) == 1,
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                    updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
                )
            }
        }
        return result
    }

    fun getSubject(subjectId: Long): Subject? = getSubjects().firstOrNull { it.id == subjectId }

    fun subjectNameExists(name: String): Boolean {
        readableDatabase.rawQuery(
            "SELECT 1 FROM subjects WHERE name = ? COLLATE NOCASE LIMIT 1",
            arrayOf(name.trim())
        ).use { return it.moveToFirst() }
    }

    fun addSubject(input: SubjectInput): Subject {
        val now = System.currentTimeMillis()
        val nextOrder = readableDatabase.rawQuery(
            "SELECT COALESCE(MAX(sort_order), -1) + 1 FROM subjects", null
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }
        val values = ContentValues().apply {
            put("name", input.name.trim())
            put("symbol", input.symbol)
            put("color", input.color)
            put("completed", 0)
            put("total", 0)
            put("sort_order", nextOrder)
            put("description", input.description.trim())
            put("cover_image", input.coverImage)
            put("is_public", if (input.isPublic) 1 else 0)
            put("created_at", now)
            put("updated_at", now)
        }
        val id = writableDatabase.insertOrThrow("subjects", null, values)
        return Subject(
            id, input.name.trim(), input.symbol, input.color, 0, 0, nextOrder,
            input.description.trim(), input.coverImage, input.isPublic, now, now
        )
    }

    fun getQuestions(subjectId: Long): List<Question> {
        val result = mutableListOf<Question>()
        readableDatabase.query(
            "questions", null, "subject_id=?", arrayOf(subjectId.toString()),
            null, null, "id ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += Question(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    subjectId = subjectId,
                    content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
                    answers = listOf(
                        cursor.getString(cursor.getColumnIndexOrThrow("answer_a")),
                        cursor.getString(cursor.getColumnIndexOrThrow("answer_b")),
                        cursor.getString(cursor.getColumnIndexOrThrow("answer_c")),
                        cursor.getString(cursor.getColumnIndexOrThrow("answer_d"))
                    ),
                    correctIndex = cursor.getInt(cursor.getColumnIndexOrThrow("correct_index")),
                    explanation = cursor.getString(cursor.getColumnIndexOrThrow("explanation"))
                )
            }
        }
        return result
    }

    fun updateProgress(subjectId: Long, completed: Int) {
        val values = ContentValues().apply { put("completed", completed) }
        writableDatabase.update("subjects", values, "id=?", arrayOf(subjectId.toString()))
    }

    fun deleteSubject(subjectId: Long): Boolean =
        writableDatabase.delete("subjects", "id=?", arrayOf(subjectId.toString())) > 0

    fun saveSubjectOrder(subjects: List<Subject>) {
        writableDatabase.beginTransaction()
        try {
            subjects.forEachIndexed { index, subject ->
                val values = ContentValues().apply { put("sort_order", index) }
                writableDatabase.update("subjects", values, "id=?", arrayOf(subject.id.toString()))
            }
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun resetLearningData() {
        writableDatabase.execSQL("DELETE FROM questions")
        writableDatabase.execSQL("DELETE FROM subjects")
        seed(writableDatabase)
    }

    private fun seed(db: SQLiteDatabase) {
        val seeds = listOf(
            SubjectSeed("Toán học", "＋", "#8750EE", "Đại số, hình học và luyện thi"),
            SubjectSeed("Vật lý", "⚛", "#14A9D8", "Cơ học, điện học và quang học"),
            SubjectSeed("Hóa học", "⚗", "#10BB8C", "Phản ứng và công thức hóa học"),
            SubjectSeed("Tiếng Anh", "GB", "#F43B61", "Từ vựng, ngữ pháp và đọc hiểu"),
            SubjectSeed("Văn học", "▤", "#FF7A12", "Tác phẩm và nghị luận văn học"),
            SubjectSeed("Lịch sử", "⌛", "#6857E8", "Sự kiện và các mốc lịch sử")
        )
        seeds.forEachIndexed { order, subject ->
            val values = ContentValues().apply {
                put("name", subject.name)
                put("symbol", subject.symbol)
                put("color", subject.color)
                put("completed", if (order == 0) 4 else if (order == 1) 3 else 0)
                put("total", 10)
                put("sort_order", order)
                put("description", subject.description)
                put("is_public", 1)
                put("created_at", System.currentTimeMillis())
                put("updated_at", System.currentTimeMillis())
            }
            val subjectId = db.insert("subjects", null, values)
            insertQuestions(db, subjectId, subject.name)
        }
    }

    private fun insertQuestions(db: SQLiteDatabase, subjectId: Long, subjectName: String) {
        val templates = questionBank[subjectName] ?: genericQuestions(subjectName)
        templates.forEach { item ->
            val values = ContentValues().apply {
                put("subject_id", subjectId)
                put("content", item[0])
                put("answer_a", item[1])
                put("answer_b", item[2])
                put("answer_c", item[3])
                put("answer_d", item[4])
                put("correct_index", item[5].toInt())
                put("explanation", item[6])
            }
            db.insert("questions", null, values)
        }
    }

    private fun genericQuestions(subject: String): List<List<String>> = (1..10).map { index ->
        listOf(
            "Câu $index: Kiến thức cơ bản nào thuộc môn $subject?",
            "Phương án A", "Phương án B", "Phương án C", "Phương án D",
            ((index - 1) % 4).toString(),
            "Đây là câu hỏi mẫu số $index của môn $subject."
        )
    }

    private data class SubjectSeed(
        val name: String,
        val symbol: String,
        val color: String,
        val description: String
    )

    companion object {
        private const val DB_NAME = "quizmaster.db"
        private const val DB_VERSION = 2

        private val questionBank = mapOf(
            "Toán học" to (1..10).map { n ->
                val a = n + 2
                listOf(
                    "Kết quả của $n + 2 bằng bao nhiêu?",
                    "${a - 1}", "$a", "${a + 1}", "${a + 2}", "1",
                    "$n + 2 = $a."
                )
            },
            "Vật lý" to listOf(
                listOf("Đơn vị SI của lực là gì?", "Joule", "Newton", "Watt", "Pascal", "1", "Lực có đơn vị là Newton (N)."),
                listOf("Công thức tính vận tốc?", "v=s/t", "v=s.t", "v=t/s", "v=s+t", "0", "Vận tốc bằng quãng đường chia thời gian."),
                listOf("Đơn vị của công suất?", "Watt", "Joule", "Newton", "Volt", "0", "Công suất có đơn vị Watt (W)."),
                listOf("Ánh sáng truyền nhanh nhất trong?", "Nước", "Thủy tinh", "Chân không", "Không khí", "2", "Vận tốc ánh sáng lớn nhất trong chân không."),
                listOf("Dụng cụ đo cường độ dòng điện?", "Vôn kế", "Ampe kế", "Ôm kế", "Nhiệt kế", "1", "Ampe kế đo cường độ dòng điện."),
                listOf("Gia tốc trọng trường gần đúng?", "9,8 m/s²", "98 m/s²", "0,98 m/s²", "8,9 m/s²", "0", "Trên mặt đất g xấp xỉ 9,8 m/s²."),
                listOf("Năng lượng không tự sinh ra và mất đi là định luật?", "Ohm", "Bảo toàn năng lượng", "Newton I", "Khúc xạ", "1", "Đó là định luật bảo toàn năng lượng."),
                listOf("Đơn vị hiệu điện thế?", "Ampere", "Volt", "Ohm", "Tesla", "1", "Hiệu điện thế có đơn vị Volt."),
                listOf("Âm thanh không truyền được trong?", "Không khí", "Nước", "Thép", "Chân không", "3", "Âm cần môi trường vật chất để truyền."),
                listOf("Kí hiệu điện trở?", "R", "I", "U", "P", "0", "Điện trở thường được kí hiệu R.")
            )
        )
    }
}
