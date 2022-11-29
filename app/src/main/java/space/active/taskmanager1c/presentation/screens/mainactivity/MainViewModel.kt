package space.active.taskmanager1c.presentation.screens.mainactivity

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.data.repository.TaskApi
import space.active.taskmanager1c.domain.models.Task
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tmpApi: TaskApi
) : ViewModel() {

    private val _testFlow = MutableStateFlow(0)
    val testFlow = _testFlow.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(true)

    private val _taskList = MutableStateFlow<List<Task>>(emptyList<Task>())

    /**
     * Variable for stoppable job witch regular update data after user login
     */
    private lateinit var updateJob: CoroutineScope

    init {

    }

    fun updateJob() {
        val coroutineContext: CoroutineContext =
            SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { context, exception -> }
        updateJob = CoroutineScope(coroutineContext)
        Log.d(TAG, "updateJob.isActive ${updateJob.isActive}")
        updateJob.launch {
            try {
                Log.d(TAG, "updateJob launch")
                while (true) {
                    /**
                    set update work here
                     */
                    getTasksFromRepository()
                    delay(1000) // TODO: delete
                }
            } catch (e: CancellationException) {
                Log.d(TAG, "updateJob Exception ${e.message}")
            } catch (e: Exception) {
                Log.d(TAG, "updateJob Exception ${e.message}")
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

    private suspend fun getTasksFromRepository() {
        tmpApi.getTaskList().collect { request ->
            when (request) {
                is SuccessRequest -> {
                    val listTaskName = request.data.tasks.map { it.name }
                    Log.d(TAG, listTaskName.joinToString("\n"))
                }
                is PendingRequest -> {
                    Log.d(TAG, "Loading")
                }
                is ErrorRequest -> {
                    if (request.exception is NullAnswerFromServer) {
                        Log.e(
                            TAG,
                            "Error: server not response. I'll try it again, but now we can show only cache"
                        )
                    } else {
                        Log.e(TAG, "Error: ${request.exception.message}")
                    }
                }
            }
        }
    }

    fun stopUpdateJob() {
        Log.d(TAG, "stopUpdateData")
        updateJob.cancel()
        Log.d(TAG, "updateJob cancel")
    }
}