package space.active.taskmanager1c.presentation.screens.mainactivity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.SaveEvents
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.HandleJobForUpdateDb
import space.active.taskmanager1c.domain.use_case.SaveNewTaskToDb
import space.active.taskmanager1c.domain.use_case.SaveTaskChangesToDb
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val handleJobForUpdateDb: HandleJobForUpdateDb,
    private val saveNewTaskToDb: SaveNewTaskToDb,
    private val saveTaskChangesToDb: SaveTaskChangesToDb,
    private val exceptionHandler: ExceptionHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger
) : ViewModel() {
    //
    private val _showSaveSnack = MutableSharedFlow<SnackBarState>()
    val showSaveSnack = _showSaveSnack.asSharedFlow()

    private val _savedTaskIdEvent = MutableSharedFlow<String>()
    val savedIdEvent = _savedTaskIdEvent.asSharedFlow()

    //
    private var cancelListener: AtomicBoolean = AtomicBoolean(false)
    private val delayedJobsList: ArrayList<SaveJob> = arrayListOf()


    val coroutineContext: CoroutineContext = SupervisorJob() + ioDispatcher

    /**
     *
     * Variable for stoppable job witch regular update data after user login
     */
    private var updateJob: Job? = null
    private val runningJob: AtomicBoolean = AtomicBoolean(false)

    //
//
    fun saveTask(saveEvents: SaveEvents) {
        // new or edit
        when (saveEvents) {
            is SaveEvents.Simple -> {
                viewModelScope.launch(SupervisorJob()) {
                    saveTaskChangesToDb(saveEvents.task)
                }
            }
            is SaveEvents.Breakable -> {
                // todo fix not break in list when open|close detailed
                val cancelDuration = saveEvents.cancelDuration
                viewModelScope.launch(SupervisorJob()) {
                    _savedTaskIdEvent.emit(saveEvents.task.id)
                    _showSaveSnack.emit(
                        SnackBarState(
                            saveEvents.task.name,
                            cancelDuration
                        )
                    )
                    //set when new start
                    cancelListener.set(false)
                    // delay for user decision
                    var timer = cancelDuration
                    while (timer > 0 && !cancelListener.get()) {
                        delay(1000)
                        timer -= 1
                    }

                    // check only after timer user decision
                    if (cancelListener.get()) {
                        logger.log(TAG, "save task break")
                        currentCoroutineContext().cancel(null)
                        cancelListener.compareAndSet(true, false)
                    } else {
                        // save changes to DB
                        saveTaskChangesToDb(saveEvents.task)
                    }
                }
            }
            is SaveEvents.Delayed -> {
                val newJob: SaveJob = SaveJob(context = CoroutineName(saveEvents.jobKey))
                val delay: Long = saveEvents.delay.toLong() * 1000
                val task = saveEvents.task
//                logger.log(TAG, "Input list: $delayedJobsList")
                if (delayedJobsList.map { it.context }.contains(newJob.context)) {
                    val filteredContext = delayedJobsList.filter { it.context == newJob.context }
                    filteredContext.forEach {
//                        logger.log(TAG, "ToCancel list: $delayedJobsList")
//                        logger.log(TAG, "Job key ${it.context} Active: ${it.job?.isActive}")
                        it.job?.cancel()
//                        logger.log(TAG, "Job key ${it.context} Active: ${it.job?.isActive}")
                    }
                }
//                logger.log(TAG, "Started list: $delayedJobsList")
                // all cancellable jobs must have supervisor for work
                newJob.job = viewModelScope.launch(newJob.context + SupervisorJob()) {

                    // Add new job if list not contains same key
                    if (!delayedJobsList.map { it.context }.contains(newJob.context)) {
                        delayedJobsList.add(newJob)
                    } else {
                        // or update job if we have it
                        delayedJobsList.map {
                            if (it.context == newJob.context) {
                                it.job = newJob.job
                            } else {
                                it
                            }
                        }
                    }
//                    logger.log(TAG, "Final list: $delayedJobsList")
                    delay(delay)
                    delayedJobsList.remove(newJob)
//                    logger.log(TAG, "Removed list: $delayedJobsList")
//                    logger.log(TAG, "Task to save: $task")
                    saveTaskChangesToDb(task)
                }
            }
            is SaveEvents.BreakSave -> {
                cancelListener.set(true)
            }
        }
    }

    //
    data class SaveJob(
        var job: Job? = null,
        val context: CoroutineContext
    )

    //
    fun updateJob() {
        logger.log(TAG, "updateJob.isActive ${updateJob?.isActive}")
        if (updateJob == null && !runningJob.get()) {
            updateJob = viewModelScope.launch(coroutineContext) {
                runningJob.compareAndSet(false, true)

                try {
                    while (true) {
//                        logger.log(TAG, "updateJob launch")
                        /**
                        set update work here
                         */
                        handleJobForUpdateDb.updateJob()
                            .catch { e ->
                                exceptionHandler(e)
                                delay(2000)
                            }
                            .collectLatest {
                                if (it is ErrorRequest) {
                                    logger.log(TAG, it.exception.message.toString())
                                }
                            }
                    }
                } catch (e: CancellationException) {
                    logger.log(TAG, "updateJob CancellationException ${e.message}")
                } catch (e: Throwable) {
                    logger.log(TAG, "updateJob Exception ${e.message}")
                    exceptionHandler(e)
                }
            }
        }
    }

    //
//    /** TODO update job
//     * - update only for authenticated user
//     * - take data only from DB
//     * - write to DB from api
//     * - update must be only in data layer with threshold handler
//     * - get and collect update result to threshold handler
//     * - catch update timeouts and tries and when the threshold is exceeded show information to user
//     */
//
    fun stopUpdateJob() {
        try {
            updateJob?.cancel()
            runningJob.compareAndSet(true, false)
            logger.log(TAG, "updateJob cancelled ${updateJob?.isActive}")
            updateJob = null
        } catch (e: UninitializedPropertyAccessException) {
            Log.w(TAG, "Warning updateJob already cancelled")
        }
    }

    //
    override fun onCleared() {
        stopUpdateJob()
        super.onCleared()
    }
}