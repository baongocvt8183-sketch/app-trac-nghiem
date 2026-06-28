package utils

fun String.toInitials(): String =
    trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .takeLast(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "QM" }
