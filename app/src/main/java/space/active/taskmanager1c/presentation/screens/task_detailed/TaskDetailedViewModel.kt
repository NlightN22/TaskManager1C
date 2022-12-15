package space.active.taskmanager1c.presentation.screens.task_detailed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.domain.models.User.Companion.toDialogItems
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.use_case.*
import javax.inject.Inject

private const val TAG = "TaskDetailedViewModel"

@HiltViewModel
class TaskDetailedViewModel @Inject constructor(
    private val repository: TasksRepository,
    private val logger: Logger,
    private val exceptionHandler: ExceptionHandler,
    private val getDetailedTask: GetDetailedTask,
    private val saveTaskChangesToDb: SaveTaskChangesToDb,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private var currentTask: Flow<Task?> = flow { emit(null) }

    private val _taskState = MutableStateFlow(TaskDetailedTaskState())
    val taskState = _taskState.asStateFlow()
    private val _expandState = MutableStateFlow(TaskDetailedExpandState())
    val expandState = _expandState.asStateFlow()

    private val _enabledFields: MutableStateFlow<EditableFields> =
        MutableStateFlow(EditableFields())
    val enabledFields = _enabledFields.asStateFlow()

    private val _showDialogEvent = MutableSharedFlow<TaskDetailedDialogs>()
    val showDialogEvent = _showDialogEvent.asSharedFlow()

    private val _saveTaskEvent = MutableSharedFlow<SaveEvents>()
    val saveTaskEvent = _saveTaskEvent.asSharedFlow()

    private val _showSnackBar = MutableSharedFlow<String>()
    val showSnackBar = _showSnackBar.asSharedFlow()

    private var saveJob: Job? = null

    // c49a0b62-c192-11e1-8a03-f46d0490adee Михайлов Олег Федорович
    val whoAmI: User = User(
        id = "c49a0b62-c192-11e1-8a03-f46d0490adee",
        name = "Михайлов Олег Федорович"
    ) // TODO replace to shared preferences

    fun saveChangesSmart(event: TaskChangesEvents) {
        viewModelScope.launch {
            var task = currentTask.first()
            val textChangeDelay = 2
            if (task != null) {
                // validation
                // save in cancellable job
                // start job with duration
                // show snackbar with duration
                //
                when (event) {
                    is TaskChangesEvents.Title -> {
                        val changes = event.title
                        if (changes != _taskState.first().title) {
                            task = task.copy(name = changes)
                            _saveTaskEvent.emit(
                                SaveEvents.Delayed(
                                    task,
                                    event.javaClass.simpleName,
                                    textChangeDelay
                                )
                            )
                        }
                    }
                    is TaskChangesEvents.EndDate -> {
                        val changes = event.toTaskDate()
                        task = task.copy(endDate = changes)
                        _saveTaskEvent.emit(SaveEvents.Simple(task))
                    }
                    is TaskChangesEvents.Performer -> {
                        val changes = event.user
                        task = task.copy(users = task.users.copy(performer = changes))
                        _saveTaskEvent.emit(SaveEvents.Simple(task))
                    }
                    is TaskChangesEvents.CoPerformers -> {
                        val changes = event.users
                        task = task.copy(users = task.users.copy(coPerformers = changes))
                        _saveTaskEvent.emit(SaveEvents.Simple(task))
                    }
                    is TaskChangesEvents.Observers -> {
                        val changes = event.users
                        task = task.copy(users = task.users.copy(observers = changes))
                        _saveTaskEvent.emit(SaveEvents.Simple(task))
                    }
                    is TaskChangesEvents.Description -> {
                        val changes = event.text
                        if (changes != _taskState.first().description) {
                            task = task.copy(description = changes)
                            _saveTaskEvent.emit(
                                SaveEvents.Delayed(
                                    task,
                                    event.javaClass.simpleName,
                                    textChangeDelay
                                )
                            )
                        }
                    }
                    is TaskChangesEvents.Status -> {
                        val cancelDuration = 5
                        val changes = event.status
                        val userIs = TaskUserIs.userIs(task, whoAmI)

                        // smart set status
                        val getStatus = GetTaskStatus()(userIs, event.status )

                        val validation = ValidationTaskChanges()(
                            changeType = event,
                            userIs = userIs,
                            getStatus
                        )
                        if (validation is ValidationResult.Success) {
                            task = task.copy(status = getStatus)
                            _saveTaskEvent.emit(SaveEvents.Breakable(task, cancelDuration))
                        } else if (validation is ValidationResult.Error) {
                            _showSnackBar.emit(validation.message)
                        }
                    }
                }
            } else {
                exceptionHandler(EmptyObject)
            }
        }
    }

    // get task flow
    fun getTaskFlow(taskId: String) {
        /**
         * If task is not new
         */
        if (taskId.isNotBlank()) {
            viewModelScope.launch(ioDispatcher) {
                currentTask = getDetailedTask(taskId)
                currentTask.collect { task ->
                    if (task != null) {
                        _taskState.value = task.toTaskState()
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
        _enabledFields.value = TaskUserIs.userIs(task, whoAmI).fields
    }

    fun showDialog(eventType: TaskDetailedDialogs) {
        viewModelScope.launch(ioDispatcher) {
            val listUsers: List<User> = repository.listUsersFlow.first()
            val currentTask = repository.getTask(_taskState.value.id).first()
            if (currentTask != null) {
                val usersIds: List<String>
                when (eventType) {
                    is PerformerDialog -> {
                        val listItems =
                            listUsers.toDialogItems(listOf(currentTask.users.performer.id))
                        _showDialogEvent.emit(PerformerDialog(listItems))
                    }
                    is CoPerformersDialog -> {
                        usersIds = currentTask.users.coPerformers.map { it.id }
                        val dialogItems = listUsers.toDialogItems(currentSelectedUsersId = usersIds)
                        _showDialogEvent.emit(CoPerformersDialog(dialogItems))
                    }
                    is ObserversDialog -> {
                        usersIds = currentTask.users.observers.map { it.id }
                        val dialogItems = listUsers.toDialogItems(currentSelectedUsersId = usersIds)
                        _showDialogEvent.emit(ObserversDialog(dialogItems))
                    }
                }
            }
        }
    }

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
}