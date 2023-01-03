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
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.use_case.*
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import javax.inject.Inject

private const val TAG = "TaskListViewModel"

@HiltViewModel
class TaskListViewModel @Inject constructor(
    userSettings: GetUserSettingsFromDataStore,
    private val repository: TasksRepository,
    private val handleEmptyTaskList: HandleEmptyTaskList,
    private val exceptionHandler: ExceptionHandler,
    private val whoUserInTask: DefineUserInTask,
    private val validate: Validate,
    logger: Logger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defDispatcher: CoroutineDispatcher,
) : BaseViewModel(userSettings, logger) {

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

    val whoAmI: Flow<User> = userSettings.getUserFlow()

    private val inputUserList = repository.listUsersFlow
    val userList: StateFlow<List<User>> =
        inputUserList.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val inputListTask: Flow<List<Task>> = combine(
        repository.listTasksFlow,
        _bottomFilter,
        _searchFilter,
        _bottomOrder
    ) { input, bottomFilter, searchFilter, bottomOrder ->
        // todo delete
//        logger.log(TAG, "_bottomFilter.combine $bottomFilter")
//        val bottomList = filterByBottom(input, bottomFilter)
//        logger.log(TAG, "_searchFilter.combine $searchFilter")
//        val searchList = if (searchFilter.isNullOrBlank()) {
//            filterByBottom(input, bottomFilter)
//        } else {
//            filterBySearch(filterByBottom(input, bottomFilter), searchFilter)
//        }
        orderByBottom(search(filterByBottom(input, bottomFilter), searchFilter), bottomOrder)
        // final result
//        val whoAmI = whoAmI.first()
//        val adapterList = orederedList.map {
//            it.toTaskAdapter(defineStatusForList(it, whoAmI), whoAmI)
//            }

    }

    // todo add precalculate in updatejob and check for null parameter
//    private fun defineStatusForList(task: Task, whoAmI: User): TasKForAdapter.Status {
//        val userIs = whoUserInTask(task, whoAmI)
//        return when (userIs) {
//            is TaskUserIs.AuthorInReviewed -> TasKForAdapter.Status.Reviewed
//            is TaskUserIs.NotAuthorOrPerformer -> TasKForAdapter.Status.Invisible
//            else -> TasKForAdapter.Status.NotReviewed
//        }
//    }

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
            handleEmptyTaskList(userSettings().first()).collect { request ->
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
        val finalRes = viewModelScope.async(defDispatcher) {
            var result: List<Task> = emptyList()
            when (order) {
                is TaskListOrderTypes.StartDate -> {
                    result = if (order.desc) {
                        list.sortedByDescending { it.date }
                    } else {
                        list.sortedBy { it.date }
                    }
                }
                is TaskListOrderTypes.EndDate -> {
                    result = if (order.desc) {
                        list.sortedByDescending { it.endDate }
                    } else {
                        list.sortedBy { it.endDate }
                    }
                }
                is TaskListOrderTypes.Name -> {
                    result = if (order.desc) {
                        list.sortedByDescending { it.name }
                    } else {
                        list.sortedBy { it.name }
                    }
                }
                is TaskListOrderTypes.Performer -> {
                    result = if (order.desc) {
                        list.sortedByDescending { it.users.performer.name }
                    } else {
                        list.sortedBy { it.users.performer.name }
                    }
                }
            }
            result
        }
        return finalRes.await()
    }


    private suspend fun filterByBottom(list: List<Task>, filter: TaskListFilterTypes): List<Task> {
        val finalRes = viewModelScope.async(defDispatcher) {
            val whoAmI = whoAmI.first()
            var result: List<Task> = emptyList()
            when (filter) {
                is TaskListFilterTypes.IDo -> {
                    result = list.filterIDo(whoAmI)
                }
                is TaskListFilterTypes.IDelegate -> {
                    result = list.filterIDelegate(whoAmI)
                }
                is TaskListFilterTypes.IDidNtCheck -> {
                    result = list.filterIDidNtCheck(whoAmI)
                }
                is TaskListFilterTypes.IObserve -> {
                    result = list.filterIObserve(whoAmI)
                }
                is TaskListFilterTypes.IDidNtRead -> {
                    result = list.filterUnread()
                }
                is TaskListFilterTypes.All -> {
                    result = list
                }
            }
            result
        }
        return finalRes.await()
    }

    private suspend fun filterBySearch(list: List<Task>, filter: String): List<Task> {
        val finalRes = viewModelScope.async(defDispatcher) {
            val filteredName = list.filter { it.name.contains(filter, true) }
            val filteredAuthor = list.filter { it.users.author.name.contains(filter, true) }
            val filteredPerformer = list.filter { it.users.performer.name.contains(filter, true) }
            val filteredCoPerformers = list.filter { task ->
                task.users.coPerformers.any { user -> user.name.contains(filter, true) }
            }
            val filteredObservers = list.filter { task ->
                task.users.observers.any { user -> user.name.contains(filter, true) }
            }
            val filteredNumber = list.filter { it.number.contains(filter, true) }
            val combineList1 = filteredName.addNotContainedFromList(filteredAuthor)
            val combineList2 = combineList1.addNotContainedFromList(filteredPerformer)
            val combineList3 = combineList2.addNotContainedFromList(filteredCoPerformers)
            val combineList4 = combineList3.addNotContainedFromList(filteredObservers)
            val combineList5 = combineList4.addNotContainedFromList(filteredNumber)
            combineList5
        }
        return finalRes.await()
    }

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