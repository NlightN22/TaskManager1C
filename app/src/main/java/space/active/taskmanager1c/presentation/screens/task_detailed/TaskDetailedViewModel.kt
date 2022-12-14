package space.active.taskmanager1c.presentation.screens.task_detailed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.coreutils.SaveRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.domain.models.User.Companion.toDialogItems
import space.active.taskmanager1c.domain.models.User.Companion.toText
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.GetDetailedTask
import space.active.taskmanager1c.domain.use_case.SaveTaskChangesToDb
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

    private val _showSaveToast = MutableSharedFlow<SnackBarState>()
    val showSaveToast = _showSaveToast.asSharedFlow()

    private var saveJob: Job? = null


    fun saveChanges(event: TaskDetailedChangesEvents) {
        viewModelScope.launch {
            var task = currentTask.first()
            if (task != null) {
                val cancelDuration = 3
                // validation
                // save in cancellable job
                // start job with duration
                // show snackbar with duration
                //
                when (event) {
                    is ChangeTaskTitle -> {
                        val changes = event.title
                        if (changes != _taskState.first().title) {
                            saveJob?.cancel()
                            saveJob = breakableSaveChanges(task, changes, cancelDuration)
                        }
                    }
                    is ChangeEndDate -> {
                        val changes = event.toTaskDate()
                        task = task.copy(endDate = changes)
                        saveChanges(task, cancelDuration)
                        _showSaveToast.emit(SnackBarState(changes, cancelDuration))
                    }
                    is ChangeTaskPerformer -> {
                        val changes = event.user
                        task = task.copy(users = task.users.copy(performer = changes))
                        saveChanges(task, cancelDuration)
                        _showSaveToast.emit(SnackBarState(changes.name, cancelDuration))
                    }
                    is ChangeTaskCoPerformers -> {
                        val changes = event.users
                        task = task.copy(users = task.users.copy(coPerformers = changes))
                        saveChanges(task, cancelDuration)
                        _showSaveToast.emit(SnackBarState(changes.toText(), cancelDuration))
                    }
                    is ChangeTaskObservers -> {
                        val changes = event.users
                        task = task.copy(users = task.users.copy(observers = changes))
                        saveChanges(task, cancelDuration)
                        _showSaveToast.emit(SnackBarState(changes.toText(), cancelDuration))
                    }
                    is ChangeTaskDescription -> {
                        val changes = event.text
                        if (changes != _taskState.first().description) {
                            task = task.copy(description = changes)
                            saveChanges(task, cancelDuration)
                            _showSaveToast.emit(SnackBarState(changes, cancelDuration))
                        }
                    }
                    is ChangeTaskStatus -> {
                        val changes = event.status
                        task = task.copy(status = changes)
                        saveChanges(task, cancelDuration)
                        _showSaveToast.emit(SnackBarState(changes.toString(), cancelDuration))
                    }
                }
            } else {
                exceptionHandler(EmptyObject)
            }
        }
    }

    private fun breakableSaveChanges(task: Task, changes: String, cancelDuration: Int) =
        viewModelScope.launch {
            delay(1000)
            val newTask = task.copy(name = changes)
            saveChanges(newTask, cancelDuration)
            _showSaveToast.emit(SnackBarState(changes, cancelDuration))
        }

    private fun saveChanges(task: Task, cancelDuration: Int) {
        viewModelScope.launch {
            saveTaskChangesToDb(cancelDuration, task).collectLatest {
                if (it is SaveRequest) {
                    logger.log(
                        TAG,
                        "Save timer: ${it.timer}"
                    )
                }
            }
        }.ensureActive()
    }

    fun cancelSave() {
        saveTaskChangesToDb.cancelSave()
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