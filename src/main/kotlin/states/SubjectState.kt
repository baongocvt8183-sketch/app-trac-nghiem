package states

import models.Subject

sealed interface SubjectState {
    data object Idle : SubjectState
    data object Loading : SubjectState
    data class Ready(val subjects: List<Subject>) : SubjectState
    data class Created(val subject: Subject, val subjects: List<Subject>) : SubjectState
    data class Error(val message: String) : SubjectState
}
