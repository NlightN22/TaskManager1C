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
import javax.inject.Inject

private const val TAG = "TaskDetailedViewModel"

@HiltViewModel
class TaskDetailedViewModel @Inject constructor(
    private val repository: TasksRepository,
    private val logger: Logger,
    private val exceptionHandler: ExceptionHandler,
    private val getDetailedTask: GetDetailedTask,
    private val getTaskMessages: GetTaskMessages,
    private val sendTaskMessages: SendTaskMessages,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _taskState = MutableStateFlow<TaskDetailedViewState>(TaskDetailedViewState.New())
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

    private val _showSnackBar = MutableSharedFlow<UiText>()
    val showSnackBar = _showSnackBar.asSharedFlow()

    private val _messagesList = MutableStateFlow<Request<List<Messages>>>(PendingRequest())
    val messageList = _messagesList.asStateFlow()

    private val _sendMessageEvent = MutableSharedFlow<StateProgress<Any>>()
    val sendMessageEvent = _sendMessageEvent.asSharedFlow()


    // c49a0b62-c192-11e1-8a03-f46d0490adee Михайлов Олег Федорович
    val whoAmI: User = User(
        id = "c49a0b62-c192-11e1-8a03-f46d0490adee",
        name = "Михайлов Олег Федорович"
    ) // TODO replace to shared preferences

    private val _inputTaskId = MutableStateFlow<String>("")

    private val currentTask: Flow<Task?> = _inputTaskId.flatMapLatest {
        if (it.isNotBlank()) {
            getDetailedTask(it)
        } else {
            flow { emit(Task.newTask(whoAmI)) }
        }
    }

    private fun collectCurrentTask() {
        viewModelScope.launch {
            logger.log(TAG, "collectCurrentTask launch")
            currentTask.collectLatest { curTask ->
                // todo delete
//                logger.log(TAG, "collectCurrentTask collect $curTask")
                if (curTask != null) {
                    if (curTask.id.isNotBlank()) {
                        // Edit
                        updateUIState(oldState = _taskState.value, newTask = curTask)
                    } else {
                        // New
                        _taskState.value = TaskDetailedViewState.New(curTask.toTaskState())
                        _enabledFields.value = TaskUserIs.Author().fields
                        _messagesList.value = SuccessRequest(emptyList<Messages>())
                    }
                } else {
                    exceptionHandler(EmptyObject("currentTask"))
                }
            }
        }
    }

    private fun updateUIState(oldState: TaskDetailedViewState, newTask: Task) {
        val newState = TaskDetailedViewState.Edit(newTask.toTaskState())
        if (oldState != newState) {
            _taskState.value = newState
            // get base and inner tasks from db
            setDependentTasks(newTask)
            if (oldState.state.performer != newState.state.performer || oldState.state.author != newState.state.author) {
                _enabledFields.value = TaskUserIs.userIs(newTask, whoAmI).fields
            }
            showMessages(newTask.id)
        }
    }

    private fun showMessages(taskId: String) {
        viewModelScope.launch {
            getTaskMessages(taskId).collect { request ->
                when (request) {
                    is PendingRequest -> {}
                    is ErrorRequest -> {
                        exceptionHandler(request.exception)
                    }
                    is SuccessRequest -> {
                        val messagesList = request.data.messages
                        val convertedMessages = messagesList
                            .toMessages(request.data.users)
                            .sortedByDescending { it.dateTime }
                        val setMyList = convertedMessages.map {
                            if (it.authorId == whoAmI.id) {
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
            val task = currentTask.first()
            if (task != null) {
                if (task.id.isNotBlank()) {
                    sendTaskMessages(task.id, text).collect { res ->
                        when (res) {
                            is PendingRequest -> {
                                _sendMessageEvent.emit(Loading())
                            }
                            is SuccessRequest -> {
                                showMessages(task.id)
                                _sendMessageEvent.emit(Success(Any())) //todo replace to update from fetch
                            }
                            else -> {}
                        }
                    }
                } else {
                    _showSnackBar.emit(UiText.Resource(R.string.error_new_task_send_message))
                }
            } else {
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

    fun saveChangesSmart(event: TaskChangesEvents) {
        viewModelScope.launch {
            var task = currentTask.first()
            val textChangeDelay = 2
            if (task != null) {
                // validation
                when (event) {
                    is TaskChangesEvents.Title -> {
                        val changes = event.title
                        if (changes != _taskState.first().state.title) {
                            task = task.copy(name = changes)

                            val validationRes = ValidateTaskChanges()
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
                        val changes = event.date
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
                        if (changes != _taskState.first().state.description) {
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
                        val getStatus = GetTaskStatus()(userIs, event.status)

                        val validation = ValidateTaskChanges()(
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
                exceptionHandler(EmptyObject("currentTask"))
            }
        }
    }

    private fun setDependentTasks(task: Task) {
        viewModelScope.launch {
            val mainTaskId = task.mainTaskId
            // todo val innerTasksId =
            if (mainTaskId.isNotBlank()) {
                val mainTask = repository.getTask(mainTaskId).first()
                if (mainTask != null) {
                    val currentstate = _taskState.value
                    when (currentstate) {
                        is TaskDetailedViewState.New -> {}
                        is TaskDetailedViewState.Edit -> {
                            _taskState.value =
                                currentstate.copy(currentstate.state.copy(mainTask = mainTask.name))
                        }
                    }
                    logger.log(TAG, "setDependentTasks ${mainTask.name}")
                    // todo clickable for open
                }
            }
            // todo add inner tasks
        }
    }

    fun showDialog(eventType: TaskDetailedDialogs) {
        viewModelScope.launch(ioDispatcher) {
            val listUsers: List<User> = repository.listUsersFlow.first()
            val currentTask = repository.getTask(_taskState.value.state.id).first()
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