package space.active.taskmanager1c.presentation.screens.tasklist

import android.text.Editable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.addNotContainedFromList
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Inject

private const val TAG = "TaskListViewModel"

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: TasksRepository,
    private val logger: Logger
) : ViewModel() {


    private val _listTask = MutableStateFlow<List<Task>>(emptyList())
    val listTask = _listTask.asStateFlow()


    private val _filter = Channel<Editable?>()

    private var searchJob: Job? = null

    // c49a0b62-c192-11e1-8a03-f46d0490adee Михайлов Олег Федорович
    val whoAmI: User = User(
        id = "c49a0b62-c192-11e1-8a03-f46d0490adee",
        name = "Михайлов Олег Федорович"
    ) // TODO replace to shared preferences

    private val inputListTask = repository.listTasksFlow

    init {
        viewModelScope.launch {
            inputListTask.collect { list ->
                logger.log(TAG, "inputListTask.collect")
                _filter.consumeEach { filter ->
                    logger.log(TAG, "_filter.collect")
                    if (filter.isNullOrBlank()) {
                        logger.log(TAG, "full list")
                        _listTask.value = list
                    } else {
                        logger.log(TAG, "filtered list")
                        val filteredName = list.filter { it.name.contains(filter, true) }
                        val filteredAuthor = list.filter { it.users.author.name.contains(filter, true) }
                        val filteredPerformer = list.filter { it.users.performer.name.contains(filter, true) }
                        val combineList1 = filteredName.addNotContainedFromList(filteredAuthor)
                        val combineList2 = combineList1.addNotContainedFromList(filteredPerformer)
                        _listTask.value = combineList2
                    }
                }
            }
        }
        // initial start filter
        viewModelScope.launch {
            _filter.send(null)
        }
    }

    fun find(expression: Editable?) {
        searchJob?.cancel()
//        logger.log(TAG, "searchJob onStart ${searchJob?.isActive}")
        searchJob = viewModelScope.launch {
            delay(500)
            _filter.send(expression)
//            logger.log(TAG, "searchJob End ${searchJob?.isActive}")
        }
    }

    private fun filterTask(listTask: List<Task>, expression: Editable?): List<Task> {
        if (expression.isNullOrBlank()) {
            logger.log(TAG, "expression isNullOrBlank")
            return listTask
        } else {
            logger.log(TAG, "listTask ${listTask.map { it.name }}")
            return listTask.filter { it.name.contains(expression.toString(), true) }
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