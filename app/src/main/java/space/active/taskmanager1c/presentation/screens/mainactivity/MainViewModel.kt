package space.active.taskmanager1c.presentation.screens.mainactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.UsersInTaskDomain
import space.active.taskmanager1c.domain.use_case.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
//    private val tmpApi: TaskApi
    private val handleJobForUpdateDb: HandleJobForUpdateDb,
    private val handleEmptyTaskList: HandleEmptyTaskList,
    private val getTaskListFromDb: GetTaskListFromDb,
    private val getDetailedTask: GetDetailedTask,
    private val saveTaskChangesToDb: SaveTaskChangesToDb,
    private val saveNewTaskToDb: SaveNewTaskToDb,
    private val logger: Logger
) : ViewModel() {

    private val _testCaseText = MutableStateFlow("")
    val testCaseText = _testCaseText.asStateFlow()

    private val _listTasks: MutableStateFlow<String> = MutableStateFlow("")
    val listTasks: StateFlow<String> = _listTasks

    private var runningJob: AtomicBoolean = AtomicBoolean(false)

    /**
     * Variable for stoppable job witch regular update data after user login
     */
    private lateinit var updateJob: CoroutineScope

    init {
        updateJob()
//        viewModelScope.launch {
//            getTaskListFromDb().collect { listTasks ->
//                when (listTasks) {
//                    is SuccessRequest -> {
//                        _listTasks.value = listTasks.data
//                    }
//                    is ErrorRequest -> {
//                        _listTasks.value = listTasks.exception.message.toString()
//                    }
//                    is PendingRequest -> {
//                        _listTasks.value = "Loading..."
//                    }
//                }
//            }
//        }
//
//        viewModelScope.launch {
//            // 3bb37cb5-a9a6-11e7-9d3f-00155d28010b
//            getDetailedTask("3bb37cb5-a9a6-11e7-9d3f-00155d28010b").collect { res ->
//                when (res) {
//                    is ErrorRequest -> {
//                        _testCaseText.value = res.exception.message.toString()
//                    }
//                    is PendingRequest -> {
//                        _testCaseText.value = "Loading..."
//                    }
//                    is SuccessRequest -> {
//                        val result = res.data.toString()
//                        logger.log(TAG, result)
//                        _testCaseText.value = result
//                    }
//                }
//            }
//
//        }
    }

    fun readTask(taskId: String) {
        viewModelScope.launch {
            // 3bb37cb5-a9a6-11e7-9d3f-00155d28010b
            getDetailedTask(taskId).collect { res ->
                when (res) {
                    is ErrorRequest -> {
                        _testCaseText.value = res.exception.message.toString()
                    }
                    is PendingRequest -> {
                        _testCaseText.value = "Loading..."
                    }
                    is SuccessRequest -> {
                        val result = res.data.toString()
                        logger.log(TAG, result)
                        _testCaseText.value = result
                    }
                }
            }

        }
    }

//    fun newTask() {
//        viewModelScope.launch {
//            saveNewTaskToDb(tesNewTask()).collect { request ->
//                when (request) {
//                    is ErrorRequest -> {
//                        _listTasks.value = request.exception.message.toString()
//                    }
//                    is PendingRequest -> {
//                        _listTasks.value = "Loading..."
//                    }
//                    is SuccessRequest -> {
//                        val result = request.data.toString()
//                        logger.log(TAG, result)
//                        _listTasks.value = "Saved!"
//                    }
//                }
//            }
//        }
//    }

//    fun editTask() {
//        viewModelScope.launch {
//            saveTaskChangesToDb(testEditTask()).collect { request ->
//                when (request) {
//                    is ErrorRequest -> {
//                        _listTasks.value = request.exception.message.toString()
//                    }
//                    is PendingRequest -> {
//                        _listTasks.value = "Loading..."
//                    }
//                    is SuccessRequest -> {
//                        val result = request.data.toString()
//                        logger.log(TAG, result)
//                        _listTasks.value = "Saved!"
//                    }
//                }
//            }
//        }
//    }

//    fun tesNewTask(): Task =
//        Task(
//            date = "2022-11-11T11:11:11",
//            description = "",
//            endDate = "",
//            id = "",
//            mainTaskId = "",
//            name = "New task ",
//            number = "",
//            objName = "",
//            photos = emptyList(),
//            priority = "middle",
//            status = Task.Status.New,
//            users = UsersInTaskDomain(
//                author = "c49a0b62-c192-11e1-8a03-f46d0490adee",
//                performer = "c49a0b62-c192-11e1-8a03-f46d0490adee",
//                coPerformers = emptyList(),
//                observers = emptyList()
//            ),
//        )

//    fun testEditTask(): Task =
//        Task(
//            date = "2017-10-05T15:21:34",
//            description = "",
//            endDate = "",
//            id = "3bb37cb5-a9a6-11e7-9d3f-00155d28010b",
//            mainTaskId = "",
//            name = "New task name in Edit Task",
//            number = "New number",
//            objName = "",
//            photos = emptyList(),
//            priority = "middle",
//            status = Task.Status.New,
//            users = UsersInTaskDomain(
//                author = "c49a0b62-c192-11e1-8a03-f46d0490adee",
//                performer = "c49a0b62-c192-11e1-8a03-f46d0490adee",
//                coPerformers = emptyList(),
//                observers = emptyList()
//            ),
//            outputId = 1
//        )

    fun updateJob() {
        val coroutineContext: CoroutineContext =
            SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { context, exception ->
                logger.log(
                    TAG,
                    "updateJob CoroutineExceptionHandler ${exception.message}"
                ) // TODO Handle exception
            }
        updateJob = CoroutineScope(coroutineContext)
        logger.log(TAG, "updateJob.isActive ${updateJob.isActive}")
        updateJob.launch {
            if (runningJob.get()) {
                logger.error(TAG, "Update Job already running")
                return@launch
            }
            runningJob.compareAndSet(false, true)
            try {
                logger.log(TAG, "updateJob launch")

                /**
                set update work here
                 */
                handleJobForUpdateDb.updateJob().collectLatest {
                    logger.log(TAG, it.toString())
                }
            } catch (e: CancellationException) {
                logger.log(TAG, "updateJob CancellationException ${e.message}")
            } catch (e: Exception) {
                logger.log(TAG, "updateJob Exception ${e.message}")
            }
        }
    }

    /** TODO update job
     * - update only for authenticated user
     * - take data only from DB
     * - write to DB from api
     * - update must be only in data layer with threshold handler
     * - get and collect update result to threshold handler
     * - catch update timeouts and tries and when the threshold is exceeded show information to user
     */

    fun stopUpdateJob() {
        updateJob.cancel()
        runningJob.compareAndSet(true, false)
        logger.log(TAG, "updateJob cancelled")
    }

    override fun onCleared() {
        stopUpdateJob()
        super.onCleared()
    }
}