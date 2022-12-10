package space.active.taskmanager1c.presentation.screens.task_detailed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.use_case.GetDetailedTask
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val TAG = "TaskDetailedViewModel"

@HiltViewModel
class TaskDetailedViewModel @Inject constructor(
    private val repository: TasksRepository,
    private val logger: Logger,
    private val getDetailedTask: GetDetailedTask
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
    fun getTaskFlow(taskId: String) {
        viewModelScope.launch {
            getDetailedTask(taskId).collect { task ->
                if (task != null) {
                    _taskState.value = _taskState.value.copy(
                        id = task.id,
                        title = task.name,
                        startDate = task.date,
                        number = task.number,
                        author = task.users.author.name,
                        deadLine = task.endDate,
                        daysEnd = task.getDeadline(),
                        performer = task.users.performer.name,
                        coPerfomers = task.users.coPerformers.toText(),
                        observers = task.users.observers.toText(),
                        description = task.description,
                        taskObject = task.objName,
                        mainTask = task.mainTaskId
                    )
                } else {
                    _taskState.value = TaskDetailedTaskState()
                }
            }
        }

    }

    // get messages flow
    // set view state:
    // NullTask, NewTask, Editable Task, InputValidationError, SaveCancelChanges
    // Expand main details
    fun expandCloseMainDetailed() {
        _expandState.value = _expandState.value.copy(main = !_expandState.value.main)
    }

    fun expandMainDetailed() {
        _expandState.value = _expandState.value.copy(main = true)
    }

    fun closeMainDetailed() {
        _expandState.value = _expandState.value.copy(main = false)
    }

    // Expand description
    fun expandCloseDescription() {
        _expandState.value =
            _expandState.value.copy(description = !_expandState.value.description)
    }

    fun expandDescription() {
        _expandState.value = _expandState.value.copy(description = true)
    }

    fun closeDescription() {
        _expandState.value = _expandState.value.copy(description = false)
    }

    private fun List<User>.toText(): String {
        if (this.isNotEmpty()) {
            return this.map { it.name }.toString().dropLast(1).drop(1)
        } else {
            return ""
        }
    }

    /**
     * Return deadline in string
     */
    private fun Task.getDeadline(): String {
        val end = this.endDate
        if (end.isNotBlank()) {
            val today = LocalDate.now().toEpochDay()
            try {
                // 2022-04-07T00:52:37
                val endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val end = endDate.toEpochDay()
                val difference: Long = end - today
                return "${difference.toString()} дней"
            } catch (e: Exception) {
                return e.message.toString()
            }
        } else {
            return ""
        }
    }
}