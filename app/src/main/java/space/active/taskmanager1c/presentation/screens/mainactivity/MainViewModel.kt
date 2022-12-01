package space.active.taskmanager1c.presentation.screens.mainactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.repository.UpdateJobHandler
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
//    private val tmpApi: TaskApi
    private val repository: TasksRepository,
    private val updateJobHandler: UpdateJobHandler
) : ViewModel() {

    @Inject
    lateinit var logger: Logger

    private var _listTasks: MutableStateFlow<String> = MutableStateFlow("")
    var listTasks: StateFlow<String> = _listTasks

    private var runningJob: AtomicBoolean = AtomicBoolean(false)

    /**
     * Variable for stoppable job witch regular update data after user login
     */
    private lateinit var updateJob: CoroutineScope

    init {

    }

    fun updateJob() {
        val coroutineContext: CoroutineContext =
            SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { context, exception ->
                logger.log(TAG, "updateJob CoroutineExceptionHandler ${exception.message}")
            }
        updateJob = CoroutineScope(coroutineContext)
        logger.log(TAG, "updateJob.isActive ${updateJob.isActive}")
        updateJob.launch {
            if (runningJob.get()) {
                logger.error(TAG, "Update Job already running")
                return@launch
            }
            runningJob.compareAndSet(false, true)
            while (true) {
                try {
                    logger.log(TAG, "updateJob launch")

                    /**
                    set update work here
                     */
                    updateJobHandler.updateJob().collectLatest {
                        logger.log(TAG, it.toString())
                    }
                } catch (e: CancellationException) {
                    logger.log(TAG, "updateJob CancellationException ${e.message}")
                } catch (e: Exception) {
                    logger.log(TAG, "updateJob Exception ${e.message}")
                }
                delay(1500) // TODO: delete
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
        logger.log(TAG, "stopUpdateData")
        updateJob.cancel()
        runningJob.compareAndSet(true, false)
        logger.log(TAG, "updateJob cancel")
    }
}