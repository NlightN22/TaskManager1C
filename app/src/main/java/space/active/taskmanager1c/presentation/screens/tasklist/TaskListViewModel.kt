package space.active.taskmanager1c.presentation.screens.tasklist

import android.text.Editable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.addNotContainedFromList
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.TaskListFilterTypes
import space.active.taskmanager1c.domain.models.TaskListFilterTypes.Companion.filterIDelegate
import space.active.taskmanager1c.domain.models.TaskListFilterTypes.Companion.filterIDidNtCheck
import space.active.taskmanager1c.domain.models.TaskListFilterTypes.Companion.filterIDo
import space.active.taskmanager1c.domain.models.TaskListFilterTypes.Companion.filterIObserve
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Inject

private const val TAG = "TaskListViewModel"

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: TasksRepository,
    private val logger: Logger
) : ViewModel() {


    private val _listTask = MutableStateFlow<Request<List<Task>>>(PendingRequest())
    val listTask = _listTask.asStateFlow()

    private val _searchFilter = MutableStateFlow<String>("")
    private val _bottomFilter = MutableStateFlow<TaskListFilterTypes>(TaskListFilterTypes.All)

    private var searchJob: Job? = null

    // c49a0b62-c192-11e1-8a03-f46d0490adee Михайлов Олег Федорович
    val whoAmI: User = User(
        id = "c49a0b62-c192-11e1-8a03-f46d0490adee",
        name = "Михайлов Олег Федорович"
    ) // TODO replace to shared preferences

    private val inputListTask = combine(repository.listTasksFlow, _bottomFilter, _searchFilter) { input, bottomFilter, searchFilter ->
//        logger.log(TAG, "_bottomFilter.combine $bottomFilter")
        val bottomList = filterByBottom(input, bottomFilter)
//        logger.log(TAG, "_searchFilter.combine $searchFilter")
        if (searchFilter.isNullOrBlank()) {
            bottomList
        } else {
            filterBySearch(bottomList, searchFilter)
        }
    }

    init {

        viewModelScope.launch {
            inputListTask.collect { inputList ->
//                logger.log(TAG, "inputListTask.collect")
                _listTask.value = SuccessRequest(inputList)
            }
        }
    }


    private fun collectBottomList() {
        viewModelScope.launch {

        }

    }

    fun filterByBottomMenu(filterType: TaskListFilterTypes) {
        viewModelScope.launch {
            _bottomFilter.value = filterType
        }
    }


    private fun filterByBottom(list: List<Task>, filter: TaskListFilterTypes): List<Task> {
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
                result = list // todo add not readed status
            }
            is TaskListFilterTypes.All -> {
                result = list
            }
        }
        return result
    }

    private fun filterBySearch(list: List<Task>, filter: String): List<Task> {
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
        return combineList5
    }

    fun find(expression: Editable?) {
        searchJob?.cancel()
//        logger.log(TAG, "searchJob onStart ${searchJob?.isActive} expr: $expression")
        searchJob = viewModelScope.launch {
            delay(500)
            _searchFilter.value = expression?.toString() ?: ""
//            logger.log(TAG, "searchJob End ${searchJob?.isActive} ${_searchFilter.value}")
        }
    }

    // start update job if user isAuth
    // open newtask
    // get ordered tasklist
    // get filtered tasklist
    // set task as completed
    // set task as not_completed
    // set task as unreadable
    // goBack


}