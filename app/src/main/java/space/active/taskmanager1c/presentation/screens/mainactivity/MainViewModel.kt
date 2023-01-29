package space.active.taskmanager1c.presentation.screens.mainactivity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.BackendException
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.SaveEvents
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.repository.UpdateJobInterface
import space.active.taskmanager1c.domain.use_case.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val updateJobInterface: UpdateJobInterface,
    private val settings: SettingsRepository,
    private val getCredentials: GetCredentials,
    private val saveTaskChangesToDb: SaveTaskChangesToDb,
    private val saveBreakable: SaveBreakable,
    private val saveDelayed: SaveDelayed,
    private val exceptionHandler: ExceptionHandler,
    private val clearAllTables: ClearAllTables,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger
) : ViewModel() {
    //
    private val _showSaveSnack = MutableSharedFlow<SnackBarState>()
    val showSaveSnack = _showSaveSnack.asSharedFlow()

    private val _savedTaskIdEvent = MutableSharedFlow<String>()
    val savedIdEvent = _savedTaskIdEvent.asSharedFlow()

    val coroutineContext: CoroutineContext = SupervisorJob() + ioDispatcher

    private val _exitEvent = MutableSharedFlow<Boolean>()
    val exitEvent = _exitEvent.asSharedFlow()

    val showExceptionDialogEvent: SharedFlow<BackendException> = exceptionHandler.sendExceptionEvent

    /**
     *
     * Variable for stoppable job witch regular update data after userDomain login
     */
    private var updateJob: Job? = null
    private val runningJob: AtomicBoolean = AtomicBoolean(false)

    fun clearAndExit() {
        stopUpdateJob()
        viewModelScope.launch {
            settings.clearSettings().collect { clearSettings ->
                when (clearSettings) {
                    is SuccessRequest -> {
                        clearAllTables().collectLatest {
                            if (it) {
                                _exitEvent.emit(true)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun saveTask(saveEvents: SaveEvents) {
        // new or edit
        when (saveEvents) {
            is SaveEvents.Simple -> {
                viewModelScope.launch(SupervisorJob()) {
                    saveTaskChangesToDb(saveEvents.taskDomain)
                }
            }
            is SaveEvents.Breakable -> {
                viewModelScope.launch(SupervisorJob()) {
                    _savedTaskIdEvent.emit(saveEvents.taskDomain.id)
                    _showSaveSnack.emit(
                        SnackBarState(
                            saveEvents.taskDomain.name,
                            saveEvents.cancelDuration
                        )
                    )
                    saveBreakable(this, saveEvents.cancelDuration, saveEvents.taskDomain)
                }
            }
            is SaveEvents.Delayed -> saveDelayed(
                viewModelScope,
                saveEvents.jobKey,
                saveEvents.taskDomain,
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
                        logger.log(TAG, "updateJob start")
                        /**
                        set update work here
                         */
                        updateJobInterface.updateJob(
                            getCredentials(), 1000L,
                            settings.getUser().toUserInput(),
                            exceptionHandler.skipBackendException
                            )
                            .catch { e ->
                                exceptionHandler(e)
                                //TODO add error counter
                                delay(5000) // pause after error
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

    fun stopUpdateJob() {
        try {
            updateJob?.cancel()
            runningJob.compareAndSet(true, false)
            logger.log(TAG, "stopUpdateJob. Job is Active: ${updateJob?.isActive}")
            updateJob = null
        } catch (e: UninitializedPropertyAccessException) {
            Log.w(TAG, "Warning updateJob already cancelled")
        }
    }

    fun skipBackendException(backendException: BackendException) {
        exceptionHandler.skipBackendExceptions(backendException)
    }

    override fun onCleared() {
        stopUpdateJob()
        super.onCleared()
    }
}