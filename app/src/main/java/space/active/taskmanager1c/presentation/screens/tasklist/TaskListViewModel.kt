package space.active.taskmanager1c.presentation.screens.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Inject

private const val TAG = "TaskListViewModel"

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: TasksRepository,
    private val logger: Logger
): ViewModel() {

    private val inputListTask = repository.listTasksFlow

    private val _listTask = MutableStateFlow<List<Task>>(emptyList())
    val listTask = _listTask.asStateFlow()

    init {
        viewModelScope.launch {
            inputListTask.collect {
                _listTask.value = it
            }
        }
    }
    // start update job if user isAuth
    // open taskedit
    // open newtask
    // get ordered tasklist
    // get filtered tasklist
    // set task as completed
    // set task as not_completed
    // set task as unreadable
    // goBack


}