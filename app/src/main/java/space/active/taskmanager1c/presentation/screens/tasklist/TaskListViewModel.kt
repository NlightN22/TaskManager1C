package space.active.taskmanager1c.presentation.screens.tasklist

import android.text.Editable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.DefaultDispatcher
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.*
import space.active.taskmanager1c.domain.models.TaskListFilterTypes.Companion.filterIDelegate
import space.active.taskmanager1c.domain.models.TaskListFilterTypes.Companion.filterIDidNtCheck
import space.active.taskmanager1c.domain.models.TaskListFilterTypes.Companion.filterIDo
import space.active.taskmanager1c.domain.models.TaskListFilterTypes.Companion.filterIObserve
import space.active.taskmanager1c.domain.models.TaskListFilterTypes.Companion.filterUnread
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.use_case.*
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import javax.inject.Inject

private const val TAG = "TaskListViewModel"

@HiltViewModel
class TaskListViewModel @Inject constructor(
    settings: SettingsRepository,
    private val getCredentials: GetCredentials,
    private val repository: TasksRepository,
    private val handleEmptyTaskList: HandleEmptyTaskList,
    private val exceptionHandler: ExceptionHandler,
    private val whoUserInTask: DefineUserInTask,
    private val validate: Validate,
    logger: Logger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defDispatcher: CoroutineDispatcher,
) : BaseViewModel(settings, logger) {

    private val _startUpdateJob = MutableStateFlow<Boolean>(false)
    val startUpdateJob = _startUpdateJob.asStateFlow()

    private val _saveTaskEvent = MutableSharedFlow<SaveEvents>()
    val saveTaskEvent = _saveTaskEvent.asSharedFlow()

    private val _incomeSavedId = MutableSharedFlow<String>() // for block tasks in list

    private val _showSnackBar = MutableSharedFlow<UiText>()
    val showSnackBar = _showSnackBar.asSharedFlow()

    private val _listTask = MutableStateFlow<Request<List<Task>>>(PendingRequest())
    val listTask = _listTask.asStateFlow()

    private val _searchFilter = MutableStateFlow<String>("")
    private val _bottomFilter = MutableStateFlow<TaskListFilterTypes>(TaskListFilterTypes.All)
    private val _bottomOrder =
        MutableStateFlow<TaskListOrderTypes>(TaskListOrderTypes.StartDate(true))
    val bottomOrder = _bottomOrder.asStateFlow()

    private var searchJob: Job? = null

    val whoAmI: Flow<User> = settings.getUserFlow()

    private val inputUserList = repository.listUsersFlow
    val userList: StateFlow<List<User>> =
        inputUserList.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val inputListTask: Flow<List<Task>> = combine(
        repository.listTasksFlow,
        _bottomFilter,
        _searchFilter,
        _bottomOrder
    ) { input, bottomFilter, searchFilter, bottomOrder ->
        orderByBottom(
            search(
                // todo fix accepted author status - not show OK
                filterByBottom(input, bottomFilter), searchFilter), bottomOrder)
    }


    private suspend fun search(filterByBottom: List<Task>, searchFilter: String): List<Task> =
        if (searchFilter.isNullOrBlank()) {
            filterByBottom
        } else {
            filterBySearch(filterByBottom, searchFilter)
        }

    fun collectListTasks() {
        logger.log(TAG, "collectListTasks")
        inputListTask.collectInScope { inputList ->
            checkForInputListAndTryFetch(inputList)
        }
    }

    private suspend fun checkForInputListAndTryFetch(inputList: List<Task>) {
        val curListIsEmpty = repository.listTasksFlow.first().isEmpty()
        if (inputList.isEmpty() && curListIsEmpty) {
            handleEmptyTaskList(getCredentials(), settings.getUser().toUserInput()).collect { request ->
                when (request) {
                    is SuccessRequest -> {
                        _listTask.value = SuccessRequest(inputList)
                        _startUpdateJob.value = true
                    }
                    is PendingRequest -> {
                        _listTask.value = PendingRequest()
                    }
                    is ErrorRequest -> {
                        exceptionHandler(request.exception)
                    }
                }
            }
        } else {
            _listTask.value = SuccessRequest(inputList)
            _startUpdateJob.value = true
        }
    }

    // todo set task as unreadable
    // todo implement
    private fun changeIsSending(list: List<Task>, taskId: String): List<Task> {
        logger.log(TAG, "_incomeSavedId.combine $taskId")
        return list.map {
            if (it.id == taskId) {
                it.copy(isSending = !it.isSending)
            } else {
                it
            }
        }
    }

    fun changeIsSending(taskId: String) {
        viewModelScope.launch {
            _incomeSavedId.emit(taskId)
        }
    }


    fun changeTaskStatus(taskIn: Task) {
        viewModelScope.launch(ioDispatcher) {
            val curTask: Task? = repository.getTask(taskIn.id).first()
            curTask?.let {
                val cancelDuration = 5
                val ok = TaskChangesEvents.Status(true)
                val userIs = whoUserInTask(it, whoAmI.first())
                // smart set status
                val taskStatus = GetTaskStatus()(userIs, ok.status)
                val validateResult = validate.okCancelChoose(
                    ok.status,
                    userIs = userIs,
                    taskStatus
                )
                if (validateResult.success) {
                    val changedTask = it.copy(status = taskStatus)
                    _saveTaskEvent.emit(SaveEvents.Breakable(changedTask, cancelDuration))
                } else {
                    validateResult.errorMessage?.let { error -> _showSnackBar.emit(error) }
                }
            } ?: kotlin.run {
                exceptionHandler(EmptyObject("task"))
            }
        }
    }


    fun orderByBottomMenu(orderTypes: TaskListOrderTypes) {
        viewModelScope.launch {
            if (orderTypes == _bottomOrder.value) {
                _bottomOrder.value = when (orderTypes) {
                    is TaskListOrderTypes.Name -> {
                        orderTypes.copy(desc = !orderTypes.desc)
                    }
                    is TaskListOrderTypes.Performer -> {
                        orderTypes.copy(desc = !orderTypes.desc)
                    }
                    is TaskListOrderTypes.StartDate -> {
                        orderTypes.copy(desc = !orderTypes.desc)
                    }
                    is TaskListOrderTypes.EndDate -> {
                        orderTypes.copy(desc = !orderTypes.desc)
                    }
                }
            } else {
                _bottomOrder.value = orderTypes
            }
        }
    }


    fun filterByBottomMenu(filterType: TaskListFilterTypes) {
        viewModelScope.launch {
            _bottomFilter.value = filterType
        }
    }

    private suspend fun orderByBottom(list: List<Task>, order: TaskListOrderTypes): List<Task> {
        return viewModelScope.async(defDispatcher) {
            return@async when (order) {
                is TaskListOrderTypes.StartDate -> {
                    if (order.desc) {
                        list.sortedByDescending { it.date }
                    } else {
                        list.sortedBy { it.date }
                    }
                }
                is TaskListOrderTypes.EndDate -> {
                    if (order.desc) {
                        list.sortedByDescending { it.endDate }
                    } else {
                        list.sortedBy { it.endDate }
                    }
                }
                is TaskListOrderTypes.Name -> {
                    if (order.desc) {
                        list.sortedByDescending { it.name }
                    } else {
                        list.sortedBy { it.name }
                    }
                }
                is TaskListOrderTypes.Performer -> {
                    if (order.desc) {
                        list.sortedByDescending { it.users.performer.name }
                    } else {
                        list.sortedBy { it.users.performer.name }
                    }
                }
            }
        }.await()
    }


    private suspend fun filterByBottom(list: List<Task>, filter: TaskListFilterTypes): List<Task> {
        return viewModelScope.async(defDispatcher) {
            return@async when (filter) {
                is TaskListFilterTypes.IDo -> {
                    list.filterIDo()
                }
                is TaskListFilterTypes.IDelegate -> {
                    list.filterIDelegate()
                }
                is TaskListFilterTypes.IDidNtCheck -> {
                    list.filterIDidNtCheck()
                }
                is TaskListFilterTypes.IObserve -> {
                    list.filterIObserve()
                }
                is TaskListFilterTypes.IDidNtRead -> {
                    list.filterUnread()
                }
                is TaskListFilterTypes.All -> {
                    list
                }
            }
        }.await()
    }

    private suspend fun filterBySearch(list: List<Task>, filter: String): List<Task> {
        return withContext(viewModelScope.coroutineContext + defDispatcher) {
            list.filter {
                it.filterByName(filter) ||
                        it.filterByAuthor(filter) ||
                        it.filterByPerformer(filter) ||
                        it.filterByCoPerformer(filter) ||
                        it.filterByObservers(filter) ||
                        it.filterByNumber(filter)
            }
        }
    }

    private fun Task.filterByName(name: String): Boolean = this.name.contains(name, true)
    private fun Task.filterByAuthor(name: String): Boolean =
        this.users.author.name.contains(name, true)

    private fun Task.filterByPerformer(name: String): Boolean =
        this.users.performer.name.contains(name, true)

    private fun Task.filterByCoPerformer(name: String): Boolean =
        this.users.coPerformers.any { user -> user.name.contains(name, true) }

    private fun Task.filterByObservers(name: String): Boolean =
        this.users.observers.any { user -> user.name.contains(name, true) }

    private fun Task.filterByNumber(number: String): Boolean = this.number.contains(number, true)


    fun find(expression: Editable?) {
        searchJob?.cancel()
//        logger.log(TAG, "searchJob onStart ${searchJob?.isActive} expr: $expression")
        searchJob = viewModelScope.launch {
            delay(200)
            _searchFilter.value = expression?.toString() ?: ""
            logger.log(TAG, "searchJob End ${searchJob?.isActive} ${_searchFilter.value}")
        }
    }

}