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
import space.active.taskmanager1c.domain.use_case.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val handleJobForUpdateDb: HandleJobForUpdateDb,
    private val saveTaskChangesToDb: SaveTaskChangesToDb,
    private val saveBreakable: SaveBreakable,
    private val saveDelayed: SaveDelayed,
    private val exceptionHandler: ExceptionHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger
) : ViewModel() {
    //
    private val _showSaveSnack = MutableSharedFlow<SnackBarState>()
    val showSaveSnack = _showSaveSnack.asSharedFlow()

    private val _savedTaskIdEvent = MutableSharedFlow<String>()
    val savedIdEvent = _savedTaskIdEvent.asSharedFlow()

    val coroutineContext: CoroutineContext = SupervisorJob() + ioDispatcher

    /**
     *
     * Variable for stoppable job witch regular update data after user login
     */
    private var updateJob: Job? = null
    private val runningJob: AtomicBoolean = AtomicBoolean(false)

    fun saveTask(saveEvents: SaveEvents) {
        // new or edit
        when (saveEvents) {
            is SaveEvents.Simple -> {
                viewModelScope.launch(SupervisorJob()) {
                    saveTaskChangesToDb(saveEvents.task)
                }
            }
            is SaveEvents.Breakable -> {
                viewModelScope.launch(SupervisorJob()) {
                    _savedTaskIdEvent.emit(saveEvents.task.id)
                    _showSaveSnack.emit(
                        SnackBarState(
                            saveEvents.task.name,
                            saveEvents.cancelDuration
                        )
                    )
                    saveBreakable(this, saveEvents.cancelDuration, saveEvents.task)
                }
            }
            is SaveEvents.Delayed -> saveDelayed(
                viewModelScope,
                saveEvents.jobKey,
                saveEvents.task,
                saveEvents.delay
            )
            is SaveEvents.BreakSave -> {
                saveBreakable.cancelListener.set(true)
            }
        }
    }

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