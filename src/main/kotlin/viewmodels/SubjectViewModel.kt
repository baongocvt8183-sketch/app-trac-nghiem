package viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Subject
import models.SubjectInput
import repositories.SubjectRepository
import states.SubjectState

class SubjectViewModel(private val repository: SubjectRepository) : ViewModel() {
    private val _state = MutableStateFlow<SubjectState>(SubjectState.Idle)
    val state: StateFlow<SubjectState> = _state.asStateFlow()

    fun loadSubjects() {
        viewModelScope.launch {
            _state.value = SubjectState.Loading
            _state.value = withContext(Dispatchers.IO) {
                runCatching { SubjectState.Ready(repository.getSubjects()) }
                    .getOrElse { SubjectState.Error(it.message ?: "Không thể tải môn học") }
            }
        }
    }

    fun createSubject(input: SubjectInput) {
        if (_state.value is SubjectState.Loading) return
        viewModelScope.launch {
            _state.value = SubjectState.Loading
            val result = withContext(Dispatchers.IO) {
                repository.addSubject(input).map { created -> created to repository.getSubjects() }
            }
            _state.value = result.fold(
                onSuccess = { (created, subjects) -> SubjectState.Created(created, subjects) },
                onFailure = { SubjectState.Error(it.message ?: "Không thể tạo môn học") }
            )
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSubject(subject.id)
            val subjects = repository.getSubjects()
            withContext(Dispatchers.Main) { _state.value = SubjectState.Ready(subjects) }
        }
    }

    fun saveOrder(subjects: List<Subject>) {
        viewModelScope.launch(Dispatchers.IO) { repository.saveOrder(subjects) }
    }

    fun consumeEvent() {
        val current = _state.value
        if (current is SubjectState.Created) _state.value = SubjectState.Ready(current.subjects)
    }
}

class SubjectViewModelFactory(private val repository: SubjectRepository) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SubjectViewModel(repository) as T
}
