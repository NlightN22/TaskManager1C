package space.active.taskmanager1c.presentation.screens.task_detailed

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Inject

private const val TAG = "TaskDetailedViewModel"

@HiltViewModel
class TaskDetailedViewModel @Inject constructor(
    private val repository: TasksRepository,
    private val logger: Logger
) : ViewModel() {

    private val _taskState = MutableStateFlow(TaskDetailedTaskState())
    val taskState = _taskState.asStateFlow()
    private val _expandState = MutableStateFlow(TaskDetailedExpandState())
    val expandState = _expandState.asStateFlow()
    private val _inputMessageState = MutableStateFlow(TaskDetailedInputMessage())
    val inputMessage = _inputMessageState.asStateFlow()
    private val _saveState = MutableStateFlow(TaskDetailedSaveChangesState())
    val saveState = _saveState.asStateFlow()

    // get task flow
    // get messages flow
    // set view state:
    // NullTask, NewTask, Editable Task, InputValidationError, SaveCancelChanges
    // Expand main details
    fun expandCloseMainDetailed () {
        _expandState.value = _expandState.value.copy(main = !_expandState.value.main)
    }
    fun expandMainDetailed () {
        _expandState.value = _expandState.value.copy(main = true)
    }
    fun closeMainDetailed() {
        _expandState.value = _expandState.value.copy(main = false)
    }
    // Expand description
    fun expandCloseDescription() {
        _expandState.value = _expandState.value.copy(description = !_expandState.value.description)
    }

    fun expandDescription () {
        _expandState.value = _expandState.value.copy(description = true)
    }

    fun closeDescription () {
        _expandState.value = _expandState.value.copy(description = false)
    }

}