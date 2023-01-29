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
    private val getUnreadListIds: GetUnreadListIds, //todo delete
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

    private val _incomeSavedId = MutableSharedFlow<String>() // for block taskDomains in list

    private val _showSnackBar = MutableSharedFlow<UiText>()
    val showSnackBar = _showSnackBar.asSharedFlow()

    private val _listTaskDomain = MutableStateFlow<Request<List<TaskDomain>>>(PendingRequest())
    val listTask = _listTaskDomain.asStateFlow()

    private val _searchFilter = MutableStateFlow<String>("")
    private val _bottomFilter = MutableStateFlow<TaskListFilterTypes>(TaskListFilterTypes.All)
    private val _bottomOrder =
        MutableStateFlow<TaskListOrderTypes>(TaskListOrderTypes.StartDate(true))
    val bottomOrder = _bottomOrder.asStateFlow()

    private var searchJob: Job? = null

    val whoAmI: Flow<UserDomain> = settings.getUserFlow()

    private val inputUserList = repository.listUsersFlow
    val userDomainList: StateFlow<List<UserDomain>> =
        inputUserList.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val filteredInput: Flow<List<TaskDomain>> = repository.getTasksFiltered(
        _bottomFilter,
        _bottomOrder,
        whoAmI.map { it.id }
    ).combine(_searchFilter) {
        input, searchFilter ->
        search(input,searchFilter)
    }

    private suspend fun search(
        filterByBottom: List<TaskDomain>,
        searchFilter: String
    ): List<TaskDomain> =
        if (searchFilter.isNullOrBlank()) {
            filterByBottom
        } else {
            filterBySearch(filterByBottom, searchFilter)
        }

    // todo need to create job handler
    var collectJob: Job? = null
    fun collectListTasks() {
        logger.log(TAG, "collectListTasks")
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            filteredInput.collect { inputList ->
                checkForInputListAndTryFetch(inputList)
            }
        }
    }

    private suspend fun checkForInputListAndTryFetch(inputList: List<TaskDomain>) {
        val curListCount: Int = repository.getInputTasksCount()
        if (inputList.isEmpty() && curListCount == 0) {
            handleEmptyTaskList(
                getCredentials(),
                settings.getUser().toUserInput()
            ).collect { request ->
                when (request) {
                    is SuccessRequest -> {
                        _listTaskDomain.value = SuccessRequest(inputList)
                        _startUpdateJob.value = true
                    }
                    is PendingRequest -> {
                        _listTaskDomain.value = PendingRequest()
                    }
                    is ErrorRequest -> {
                        exceptionHandler(request.exception)
                    }
                }
            }
        } else {
            logger.log(TAG, "inputList.size ${inputList.size}")
            _listTaskDomain.value = SuccessRequest(inputList)
            _startUpdateJob.value = true
        }
    }
    // todo set taskDomain as unreadable
    // todo implement
    private fun changeIsSending(list: List<TaskDomain>, taskId: String): List<TaskDomain> {
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

    fun changeTaskStatus(taskDomainIn: TaskDomain) {
        viewModelScope.launch(ioDispatcher) {
            val curTaskDomain: TaskDomain? = repository.getTask(taskDomainIn.id).first()
            curTaskDomain?.let {
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
                exceptionHandler(EmptyObject("taskDomain"))
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

    private suspend fun filterBySearch(list: List<TaskDomain>, filter: String): List<TaskDomain> {
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

    private fun TaskDomain.filterByName(name: String): Boolean = this.name.contains(name, true)
    private fun TaskDomain.filterByAuthor(name: String): Boolean =
        this.users.author.name.contains(name, true)

    private fun TaskDomain.filterByPerformer(name: String): Boolean =
        this.users.performer.name.contains(name, true)

    private fun TaskDomain.filterByCoPerformer(name: String): Boolean =
        this.users.coPerformers.any { user -> user.name.contains(name, true) }

    private fun TaskDomain.filterByObservers(name: String): Boolean =
        this.users.observers.any { user -> user.name.contains(name, true) }

    private fun TaskDomain.filterByNumber(number: String): Boolean =
        this.number.contains(number, true)

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