package space.active.taskmanager1c.presentation.screens.task_detailed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
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
    private val getDetailedTask: GetDetailedTask,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _taskState = MutableStateFlow(TaskDetailedTaskState())
    val taskState = _taskState.asStateFlow()
    private val _expandState = MutableStateFlow(TaskDetailedExpandState())
    val expandState = _expandState.asStateFlow()
    private val _inputMessageState = MutableStateFlow(TaskDetailedInputMessage())
    val inputMessage = _inputMessageState.asStateFlow()
    private val _changeState = MutableStateFlow(TaskIsChanged())
    val changeState = _changeState.asStateFlow()
    private val _enabledFields: MutableStateFlow<EditableFields> =
        MutableStateFlow(EditableFields())
    val enabledFields = _enabledFields.asStateFlow()

    private val _showUsersToSelect = MutableSharedFlow<List<User>>()
    val  showUsersToSelect = _showUsersToSelect.asSharedFlow()

    // get task flow
    fun getTaskFlow(taskId: String) {
        /**
         * If task is not new
         */
        if (taskId.isNotBlank()) {
            viewModelScope.launch(ioDispatcher) {
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
                        setFieldsState(task)
                    }
                    /**
                     * If cant get task from DB
                     */
                    else {
                        _taskState.value = TaskDetailedTaskState()
                    }
                }
            }
        }
        /**
         * If new task
         */
        else {
            _taskState.value = TaskDetailedTaskState()
        }
    }


    private suspend fun setFieldsState(task: Task) {
        // c49a0b62-c192-11e1-8a03-f46d0490adee Михайлов Олег Федорович
        val whoAmI: User = User(
            id = "c49a0b62-c192-11e1-8a03-f46d0490adee",
            name = "Михайлов Олег Федорович"
        ) // TODO replace to shared preferences
        if (task.users.author == whoAmI) {
            _enabledFields.value = TaskUserIs.Author().fields
        } else if (task.users.performer == whoAmI && task.users.author != whoAmI) {
            _enabledFields.value = TaskUserIs.Performer().fields
        } else {
            _enabledFields.value = TaskUserIs.NotAuthorOrPerformer().fields
        }
    }

    // get messages flow

    fun showDialogSelectUsers() {
        viewModelScope.launch(ioDispatcher) {
            val listUsers = repository.listUsersFlow.first()
            _showUsersToSelect.emit(listUsers)
        }
    }
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
     * Return days deadline in string
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