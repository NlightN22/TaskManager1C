package space.active.taskmanager1c.presentation.screens.task_detailed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.*
import space.active.taskmanager1c.domain.models.Messages.Companion.toMessages
import space.active.taskmanager1c.domain.models.User.Companion.toDialogItems
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.use_case.*
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import javax.inject.Inject

private const val TAG = "TaskDetailedViewModel"

@HiltViewModel
class TaskDetailedViewModel @Inject constructor(
    private val repository: TasksRepository,
    logger: Logger,
    userSettings: GetUserSettingsFromDataStore,
    private val saveNewTaskToDb: SaveNewTaskToDb,
    private val validate: Validate,
    private val exceptionHandler: ExceptionHandler,
    private val getDetailedTask: GetDetailedTask,
    private val getTaskMessages: GetTaskMessages,
    private val sendTaskMessages: SendTaskMessages,
    private val whoUserInTask: DefineUserInTask,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel(userSettings, logger) {

    private val _taskState = MutableStateFlow<TaskDetailedViewState>(TaskDetailedViewState.New())
    val taskState = _taskState.asStateFlow()

    private val _taskErrorState = MutableSharedFlow<TaskDetailedErrorState>()
    val taskErrorState = _taskErrorState.asSharedFlow()

    private val _validationEvent = MutableSharedFlow<Boolean>()
    val validationEvent = _validationEvent.asSharedFlow()

    private val _expandState = MutableStateFlow(TaskDetailedExpandState())
    val expandState = _expandState.asStateFlow()

    private val _enabledFields: MutableStateFlow<EditableFields> =
        MutableStateFlow(EditableFields())
    val enabledFields = _enabledFields.asStateFlow()

    private val _showDialogEvent = MutableSharedFlow<TaskDetailedDialogs>()
    val showDialogEvent = _showDialogEvent.asSharedFlow()

    private val _saveTaskEvent = MutableSharedFlow<SaveEvents>()
    val saveTaskEvent = _saveTaskEvent.asSharedFlow()

    private val _showSnackBar = MutableSharedFlow<UiText>()
    val showSnackBar = _showSnackBar.asSharedFlow()

    private val _messagesList = MutableStateFlow<Request<List<Messages>>>(PendingRequest())
    val messageList = _messagesList.asStateFlow()

    private val _sendMessageEvent = MutableSharedFlow<StateProgress<Any>>()
    val sendMessageEvent = _sendMessageEvent.asSharedFlow()

    private val whoAmI: Flow<User> = userSettings.getUserFlow()

    private val _inputTaskId = MutableStateFlow<String>("")

    private val _changedNewTask = MutableStateFlow<Task?>(null)
    private val _newTask: Flow<Task> = whoAmI.map {
//        logger.log(TAG, "whoAmI.map")
        Task.newTask(it)
    }.combine(_changedNewTask) { inTask, changedTask ->
        if (changedTask == null) {
//            logger.log(TAG, "collect newTask")
            inTask
        } else {
//            logger.log(TAG, "collect _changed")
            changedTask
        }
    }

    private val currentTask: Flow<Task?> = _inputTaskId.flatMapLatest {
        if (it.isNotBlank()) {
//            logger.log(TAG, "collect from DB")
            getDetailedTask(it)
        } else {
//            logger.log(TAG, "collect from mutable")
            _newTask
        }
    }

    // view state for new task
    // bottom menu with save cancel
    // another behavior model for save changes
    // save only on buttons
    // preset for some fields

    private fun collectCurrentTask() {
        viewModelScope.launch {
            logger.log(TAG, "collectCurrentTask launch")
            currentTask.collectLatest { curTask ->
                curTask?.let {
                    updateUIState(oldState = _taskState.value, newTask = curTask)
                } ?: kotlin.run {
                    exceptionHandler(EmptyObject("currentTask"))
                }
            }
        }
    }

    private fun updateUIState(oldState: TaskDetailedViewState, newTask: Task) {
        viewModelScope.launch {
            if (newTask.id.isNotBlank()) {
                val newState = TaskDetailedViewState.Edit(newTask.toTaskState())
                logger.log(TAG, "Update Edit")
                if (oldState != newState) {
                    _taskState.value = newState
                    // get base and inner tasks from db
                    setDependentTasks(newTask)
                    if (oldState.state.performer != newState.state.performer || oldState.state.author != newState.state.author) {
                        _enabledFields.value = whoUserInTask(newTask, whoAmI.first()).fields
                    }
                    showMessages(newTask.id)
                }
            } else {
                val newState = TaskDetailedViewState.New(newTask.toTaskState())
                if (oldState != newState) {
                    logger.log(TAG, "Update new")
                    _taskState.value = newState
                    _enabledFields.value = TaskUserIs.AuthorInNewTask().fields
                    _messagesList.value = SuccessRequest(emptyList<Messages>())
                }
            }
        }
    }

    private fun showMessages(taskId: String) {
        viewModelScope.launch {
            getTaskMessages(userSettings().first(), taskId).collect { request ->
                when (request) {
                    is PendingRequest -> {}
                    is ErrorRequest -> {
                        exceptionHandler(request.exception)
                    }
                    is SuccessRequest -> {
                        val messagesList = request.data.messages
                        val convertedMessages =
                            messagesList.toMessages(request.data.users, request.data.readingTime)
                                .sortedByDescending { it.dateTime }
                        val setMyList = convertedMessages.map {
                            if (it.authorId == whoAmI.first().id) {
                                it.copy(my = true)
                            } else {
                                it.copy(my = false)
                            }
                        }
                        _messagesList.value = SuccessRequest(setMyList)
                    }
                }
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val curTask = currentTask.first()
            curTask?.let { task ->
                if (task.id.isNotBlank()) {
                    sendTaskMessages(userSettings().first(), task.id, text).collect { res ->
                        when (res) {
                            is PendingRequest -> {
                                _sendMessageEvent.emit(Loading())
                            }
                            is SuccessRequest -> {
//                                logger.log(TAG, res.data.messages.toString())
                                showMessages(task.id)
                                _sendMessageEvent.emit(Success(Any()))
                            }
                            else -> {
                                _sendMessageEvent.emit(Success(Any()))
                            }
                        }
                    }
                } else {
                    _showSnackBar.emit(UiText.Resource(R.string.error_new_task_send_message))
                }
            } ?: kotlin.run {
                exceptionHandler(EmptyObject("currentTask"))
            }
        }
    }

    fun setTaskFlow(taskId: String) {
        logger.log(TAG, "_inputTaskId $taskId")
        viewModelScope.launch {
            _inputTaskId.emit(taskId)
        }
        collectCurrentTask()
    }

    fun saveNewTask() {
        viewModelScope.launch {
            val curTask = currentTask.first()
            curTask?.let { task ->
                val titleResult = validate.title(task.name)
                val endDateResult = validate.endDate(task.endDate)
                val authorResult = validate.author(task.users.author)
                val performerResult = validate.performer(task.users.performer)
                val listRes = listOf(
                    titleResult, endDateResult, authorResult, performerResult
                )
                val hasError = listRes.any { !it.success }

                _taskErrorState.emit(
                    TaskDetailedErrorState(
                        title = titleResult.errorMessage,
                        endDate = endDateResult.errorMessage,
                        author = authorResult.errorMessage,
                        performer = performerResult.errorMessage
                    )
                )
                if (hasError) {

                    return@launch
                }
                saveNewTaskToDb(task).collect { request ->
                    when (request) {
                        is SuccessRequest -> _validationEvent.emit(true)
                        is ErrorRequest -> exceptionHandler(request.exception)
                        is PendingRequest -> {}
                    }
                }
            }
        }
    }

    fun saveEditChanges(saveEvents: TaskChangesEvents) {
        viewModelScope.launch {
            val res = validateChangeEvents(saveEvents)
            if (_taskState.value is TaskDetailedViewState.Edit) {
                res?.let { _saveTaskEvent.emit(res) }
            }
        }
    }

    /**
     * Return validated Save Event
     */
    private suspend fun validateChangeEvents(event: TaskChangesEvents): SaveEvents? {
        var saveEvent: SaveEvents? = null
        var curTask: Task? = currentTask.first()
        val textChangeDelay = 1
        curTask?.let { task ->
            var changedTask: Task = task
            // validation
            when (event) {
                is TaskChangesEvents.Title -> {
                    val changes = event.title
                    if (changes != _taskState.first().state.title) {
                        val validateResult = validate.title(event.title)
                        when (validateResult.success) {
                            true -> {
                                changedTask = task.copy(name = changes)
                                saveEvent = SaveEvents.Delayed(
                                    changedTask, event.javaClass.simpleName, textChangeDelay
                                )
                            }
                            false -> {
                                _taskErrorState.emit(TaskDetailedErrorState(title = validateResult.errorMessage))
                            }
                        }
                    }
                }
                is TaskChangesEvents.EndDate -> {
                    val changes = event.date
                    changedTask = task.copy(endDate = changes)
                    saveEvent = SaveEvents.Simple(changedTask)
                }
                is TaskChangesEvents.Performer -> {
                    val changes = event.user
                    changedTask = task.copy(users = task.users.copy(performer = changes))
                    saveEvent = SaveEvents.Simple(changedTask)
                }
                is TaskChangesEvents.CoPerformers -> {
                    val changes = event.users
                    changedTask = task.copy(users = task.users.copy(coPerformers = changes))
                    saveEvent = SaveEvents.Simple(changedTask)
                }
                is TaskChangesEvents.Observers -> {
                    val changes = event.users
                    changedTask = task.copy(users = task.users.copy(observers = changes))
                    saveEvent = SaveEvents.Simple(changedTask)
                }
                is TaskChangesEvents.Description -> {
                    val changes = event.text
                    if (changes != _taskState.first().state.description) {
                        changedTask = task.copy(description = changes)
                        saveEvent = SaveEvents.Delayed(
                            changedTask, event.javaClass.simpleName, textChangeDelay
                        )
                    }
                }
                is TaskChangesEvents.Status -> {
                    val cancelDuration = 5
                    val userIs = whoUserInTask(task, whoAmI.first())
                    // smart set status
                    val taskStatus = GetTaskStatus()(userIs, event.status)

                    val validationResult = validate.okCancelChoose(
                        event.status, userIs = userIs, taskStatus
                    )
                    when (validationResult.success) {
                        true -> {
                            changedTask = task.copy(status = taskStatus)
                            saveEvent = SaveEvents.Breakable(changedTask, cancelDuration)
                        }
                        false -> {
                            validationResult.errorMessage?.let { _showSnackBar.emit(it) }
                        }
                    }
                }
            }
//            logger.log(TAG, "Task after changes: ${task}")
            _changedNewTask.emit(changedTask)
        } ?: kotlin.run {
            exceptionHandler(EmptyObject("currentTask"))
        }
        return saveEvent
    }

    private fun setDependentTasks(task: Task) {
        viewModelScope.launch {
            val mainTaskId = task.mainTaskId
            // todo val innerTasksId =
            if (mainTaskId.isNotBlank()) {
                val mainTask = repository.getTask(mainTaskId).first()
                mainTask?.let {
                    val currentstate = _taskState.value
                    when (currentstate) {
                        is TaskDetailedViewState.New -> {}
                        is TaskDetailedViewState.Edit -> {
                            _taskState.value =
                                currentstate.copy(currentstate.state.copy(mainTask = it.name))
                        }
                    }
                    logger.log(TAG, "setDependentTasks ${it.name}")
                    // todo clickable for open
                }
            }
            // todo add inner tasks
        }
    }

    fun showDialog(eventType: TaskDetailedDialogs) {
        viewModelScope.launch(ioDispatcher) {
            val listUsers: List<User> = repository.listUsersFlow.first()
            val curTask: Task? = currentTask.first()
            curTask?.let {  task ->
                val usersIds: List<String>
                when (eventType) {
                    is PerformerDialog -> {
                        val listItems = listUsers.toDialogItems(listOf(task.users.performer.id))
                        _showDialogEvent.emit(PerformerDialog(listItems))
                    }
                    is CoPerformersDialog -> {
                        usersIds = task.users.coPerformers.map { it.id }
                        val dialogItems = listUsers.toDialogItems(currentSelectedUsersId = usersIds)
                        _showDialogEvent.emit(CoPerformersDialog(dialogItems))
                    }
                    is ObserversDialog -> {
                        usersIds = task.users.observers.map { it.id }
                        val dialogItems = listUsers.toDialogItems(currentSelectedUsersId = usersIds)
                        _showDialogEvent.emit(ObserversDialog(dialogItems))
                    }
                    is DatePicker -> {
                        _showDialogEvent.emit(DatePicker)
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
        _expandState.value = _expandState.value.copy(description = !_expandState.value.description)
    }

    fun expandDescription() {
        _expandState.value = _expandState.value.copy(description = true)
    }

    fun closeDescription() {
        _expandState.value = _expandState.value.copy(description = false)
    }
}