package space.active.taskmanager1c.presentation.screens.task_detailed

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.*
import space.active.taskmanager1c.domain.models.Messages.Companion.toMessages
import space.active.taskmanager1c.domain.models.UserDomain.Companion.toDialogItems
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.use_case.*
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import space.active.taskmanager1c.presentation.utils.EditTextDialogStates
import space.active.taskmanager1c.presentation.utils.TaskStatusDialog
import java.time.LocalDateTime
import javax.inject.Inject

private const val TAG = "TaskDetailedVM"

@HiltViewModel
class TaskDetailedViewModel @Inject constructor(
    private val repository: TasksRepository,
    logger: Logger,
    settings: SettingsRepository,
    private val getCredentials: GetCredentials,
    private val saveNewTaskToDb: SaveNewTaskToDb,
    private val validate: Validate,
    private val exceptionHandler: ExceptionHandler,
    private val getTaskMessages: GetTaskMessages,
    private val sendTaskMessages: SendTaskMessages,
    private val setTaskAndMessageReadingTime: SetTaskAndMessageReadingTime,
    private val whoUserInTask: DefineUserInTask,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel(settings, logger) {

    private val _taskState = MutableStateFlow<TaskState>(TaskState())
    val taskState = _taskState.asStateFlow()

    private val _taskErrorState = MutableSharedFlow<TaskDetailedErrorState>()
    val taskErrorState = _taskErrorState.asSharedFlow()

    private val _validationEvent = MutableSharedFlow<Boolean>()
    val validationEvent = _validationEvent.asSharedFlow()

    private val _statusAlertEvent =
        MutableSharedFlow<Pair<TaskChangesEvents.Status, TaskStatusDialog.DialogParams>>()
    val statusAlertEvent = _statusAlertEvent.asSharedFlow()

    private val _expandState = MutableStateFlow(TaskDetailedExpandState())
    val expandState = _expandState.asStateFlow()

    private val _enabledFields: MutableStateFlow<EditableFields> =
        MutableStateFlow(EditableFields())
    val enabledFields = _enabledFields.asStateFlow()

    private val _showDialogEvent = MutableSharedFlow<TaskDetailedDialogs>()
    val showDialogEvent = _showDialogEvent.asSharedFlow()

    private val _saveNewTaskEvent = MutableSharedFlow<SaveEvents>()
    val saveNewTaskEvent = _saveNewTaskEvent.asSharedFlow()

    private val _showSnackBar = MutableSharedFlow<UiText>()
    val showSnackBar = _showSnackBar.asSharedFlow()

    private val _messagesList = MutableStateFlow<Request<List<Messages>>>(PendingRequest())
    val messageList = _messagesList.asStateFlow()

    private val _sendMessageEvent = MutableSharedFlow<StateProgress<Any>>()
    val sendMessageEvent = _sendMessageEvent.asSharedFlow()

    private val whoAmI: Flow<UserDomain> = settings.getUserFlow()

    private val _inputTaskId = MutableStateFlow<String>("")

    private val _changedNewTaskDomain = MutableStateFlow<TaskDomain?>(null)
    private val _newTaskDomain: Flow<TaskDomain> = whoAmI.map {
        logger.log(TAG, "whoAmI.map")
        TaskDomain.newTask(it)
    }.combine(_changedNewTaskDomain) { inTask, changedTask ->
        if (changedTask == null) {
            logger.log(TAG, "collect newTask")
            inTask
        } else {
            logger.log(TAG, "collect _changed")
            changedTask
        }
    }

    private val currentTaskDomain: Flow<TaskDomain?> = _inputTaskId.flatMapLatest { taskId ->
        if (taskId.isNotBlank()) {
            logger.log(TAG, "collect from DB")
            repository.getTask(taskId)
        } else {
            logger.log(TAG, "collect from mutable")
            _newTaskDomain
        }
    }

    private fun collectCurrentTask() {
        viewModelScope.launch {
            logger.log(TAG, "collectCurrentTask launch")
            currentTaskDomain.collectLatest { curTask ->
                curTask?.let {
                    updateUIState(oldState = _taskState.value, newTaskDomain = curTask)
                } ?: kotlin.run {
                    exceptionHandler(EmptyObject("currentTaskDomain"))
                }
            }
        }
    }

    private fun updateUIState(oldState: TaskState, newTaskDomain: TaskDomain) {
        viewModelScope.launch {
            if (newTaskDomain.id.isNotBlank()) {
                val newState = newTaskDomain.toTaskState()
                if (oldState != newState) {
                    logger.log(TAG, "oldState ${oldState.title} newState ${newState.title}")

//                    logger.log(TAG, "Update Edit: ${newTaskDomain.toString().replace(", ", "\n")}")
                    _taskState.value = newState
                    // get base and inner taskDomains from db
                    setDependentTasks(newTaskDomain)
                    if (oldState.performer != newState.performer || oldState.author != newState.author) {
                        _enabledFields.value = whoUserInTask(newTaskDomain, whoAmI.first()).fields
                    }
                    showMessages(newTaskDomain.id)
                }
            } else {
                val newState = newTaskDomain.toTaskState()
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
            getTaskMessages(getCredentials(), taskId).collect { request ->
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
                        val setMyList: List<Messages> = convertedMessages.map {
                            if (it.authorId == whoAmI.first().id) {
                                it.copy(my = true)
                            } else {
                                it.copy(my = false)
                            }
                        }
                        sendReadingTime(taskId, setMyList)
                        _messagesList.value = SuccessRequest(setMyList)
                    }
                }
            }
        }
    }

    private fun sendReadingTime(taskId: String, messageList: List<Messages>) {
        viewModelScope.launch {
            val taskReadingTime = LocalDateTime.now()
            if (messageList.isNotEmpty()) {
                val lastMessageTime: LocalDateTime = messageList.maxBy { it.dateTime }.dateTime.toLocalDateTime()
                logger.log(TAG, "lastMessageTime: $lastMessageTime")
                setTaskAndMessageReadingTime(
                    credentials = getCredentials(),
                    taskId = taskId,
                    lastMessageTime,
                    taskReadingTime
                ).collect { request ->
                    when (request) {
                        is SuccessRequest -> {
                            delay(1000)
                            _messagesList.value =
                                SuccessRequest(messageList.map { it.copy(unread = false) })
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val curTask = currentTaskDomain.first()
            curTask?.let { task ->
                if (task.id.isNotBlank()) {
                    sendTaskMessages(getCredentials(), task.id, text).collect { res ->
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
                exceptionHandler(EmptyObject("currentTaskDomain"))
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
            val curTask = currentTaskDomain.first()
            curTask?.let { task ->
                val titleResult = validate.title(task.name)
                val endDateResult = validate.endDate(task.deadline)
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
                saveNewTaskToDb(task, whoAmI.first().id).collect { request ->
                    when (request) {
                        is SuccessRequest -> _validationEvent.emit(true)
                        is ErrorRequest -> exceptionHandler(request.exception)
                        is PendingRequest -> {}
                    }
                }
            }
        }
    }

    fun checkStatusDialog(saveEvents: TaskChangesEvents.Status) {
        viewModelScope.launch(ioDispatcher) {
            if (settings.getSkipStatusAlert()) {
                saveEditChanges(saveEvents)
            } else {
                val curTaskDomain: TaskDomain? = currentTaskDomain.first()
                curTaskDomain?.let {
                    val userIs = whoUserInTask(it, whoAmI.first())
                    val newTaskStatus = GetTaskStatus()(userIs, saveEvents.status)
                    _statusAlertEvent.emit(
                        Pair(
                            saveEvents,
                            TaskStatusDialog.DialogParams(it.name, newTaskStatus)
                        )
                    )
                }
            }
        }
    }

    fun saveEditChanges(saveEvents: TaskChangesEvents) {
        viewModelScope.launch {
            val res = validateChangeEvents(saveEvents)
            if (_taskState.value.id.isNotBlank()) {
                res?.let {
                    _saveNewTaskEvent.emit(res)
                }
            }
        }
    }

    /**
     * Return validated Save Event
     */
    private suspend fun validateChangeEvents(event: TaskChangesEvents): SaveEvents? {
        var saveEvent: SaveEvents? = null
        val curTaskDomain: TaskDomain? = currentTaskDomain.first()
        val textChangeDelay = 1
        curTaskDomain?.let { task ->
            var changedTaskDomain: TaskDomain = task
            // validation
            when (event) {
                is TaskChangesEvents.Title -> {
                    val changes = event.title
                    if (changes != _taskState.first().title) {
                        val validateResult = validate.title(event.title)
                        when (validateResult.success) {
                            true -> {
                                changedTaskDomain = task.copy(name = changes)
                                saveEvent = SaveEvents.Delayed(
                                    changedTaskDomain, event.javaClass.simpleName, textChangeDelay
                                )
                                val currentState = _taskState.value
                                _taskState.value = currentState.copy(title = changes)
                            }
                            false -> {
                                _taskErrorState.emit(TaskDetailedErrorState(title = validateResult.errorMessage))
                            }
                        }
                    }
                }
                is TaskChangesEvents.DeadLine -> {
                    val changes = event.date
                    changedTaskDomain = task.copy(deadline = changes)
                    saveEvent = SaveEvents.Simple(changedTaskDomain)
                }
                is TaskChangesEvents.Performer -> {
                    val changes = event.userDomain
                    changedTaskDomain = task.copy(users = task.users.copy(performer = changes))
                    saveEvent = SaveEvents.Simple(changedTaskDomain)
                }
                is TaskChangesEvents.CoPerformers -> {
                    val changes = event.userDomains
                    changedTaskDomain = task.copy(users = task.users.copy(coPerformers = changes))
                    saveEvent = SaveEvents.Simple(changedTaskDomain)
                }
                is TaskChangesEvents.Observers -> {
                    val changes = event.userDomains
                    changedTaskDomain = task.copy(users = task.users.copy(observers = changes))
                    saveEvent = SaveEvents.Simple(changedTaskDomain)
                }
                is TaskChangesEvents.Description -> {
                    val changes = event.text
                    if (changes != _taskState.first().description) {
                        changedTaskDomain = task.copy(description = changes)
                        saveEvent = SaveEvents.Delayed(
                            changedTaskDomain, event.javaClass.simpleName, textChangeDelay
                        )
                        val currentState = _taskState.value
                        _taskState.value = currentState.copy(description = changes)
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
                            changedTaskDomain = task.copy(status = taskStatus)
                            saveEvent = SaveEvents.Breakable(changedTaskDomain, cancelDuration)
                        }
                        false -> {
                            validationResult.errorMessage?.let { _showSnackBar.emit(it) }
                        }
                    }
                }
            }
//            logger.log(TAG, "TaskDomain after changes: ${taskDomain}")
            _changedNewTaskDomain.emit(changedTaskDomain)
        } ?: kotlin.run {
            exceptionHandler(EmptyObject("currentTaskDomain"))
        }
        return saveEvent
    }

    private fun setDependentTasks(taskDomain: TaskDomain) {
        viewModelScope.launch {
            val mainTaskId = taskDomain.mainTaskId
            if (mainTaskId.isNotBlank()) {
                val mainTask = repository.getTask(mainTaskId).first()
                mainTask?.let {
                    val currentState = _taskState.value
                    if (currentState.id.isNotBlank()) {
                        _taskState.value = currentState.copy(mainTask = it.name)
                    }
                    logger.log(TAG, "setDependentTasks ${it.name}")
                    // todo clickable for open
                }
            }
            // todo add inner taskDomains
        }
    }

    fun showDialog(eventType: TaskDetailedDialogs) {
        viewModelScope.launch(ioDispatcher) {
            val listUserDomains: List<UserDomain> = repository.listUsersFlow.first()
            val curTaskDomain: TaskDomain? = currentTaskDomain.first()
            curTaskDomain?.let { task ->
                val usersIds: List<String>
                when (eventType) {
                    is PerformerDialog -> {
                        if (!_enabledFields.value.performer) { return@launch }
                        val listItems = listUserDomains.toDialogItems(listOf(task.users.performer.id))
                        _showDialogEvent.emit(PerformerDialog(listItems))
                    }
                    is CoPerformersDialog -> {
                        if (!_enabledFields.value.coPerfomers) { return@launch }
                        usersIds = task.users.coPerformers.map { it.id }
                        val dialogItems = listUserDomains.toDialogItems(currentSelectedUsersId = usersIds)
                        _showDialogEvent.emit(CoPerformersDialog(dialogItems))
                    }
                    is ObserversDialog -> {
                        if (!_enabledFields.value.observers) { return@launch }
                        usersIds = task.users.observers.map { it.id }
                        val dialogItems = listUserDomains.toDialogItems(currentSelectedUsersId = usersIds)
                        _showDialogEvent.emit(ObserversDialog(dialogItems))
                    }
                    is DatePicker -> {
                        if (!_enabledFields.value.deadLine) { return@launch }
                        _showDialogEvent.emit(DatePicker)
                    }
                    is EditTitleDialog -> {
                        if (!_enabledFields.value.title) { return@launch }
                        val currentTitle = curTaskDomain.name
                        _showDialogEvent.emit(
                            EditTitleDialog(
                                EditTextDialogStates(
                                    hint = R.string.title_dialog_hint,
                                    text = currentTitle,
                                    maxLength = R.integer.title_max_length,
                                )
                            )
                        )
                    }
                    is EditDescriptionDialog -> {
                        if (!_enabledFields.value.description) { return@launch }
                        val currentDescription = curTaskDomain.description
                            _showDialogEvent.emit(
                                EditDescriptionDialog(
                                    EditTextDialogStates(
                                        hint = R.string.description_dialog_hint,
                                        text = currentDescription,
                                    )
                                )
                            )
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