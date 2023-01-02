package space.active.taskmanager1c.presentation.screens.mainactivity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.SaveEvents
import space.active.taskmanager1c.domain.models.UserSettings
import space.active.taskmanager1c.domain.repository.UpdateJobInterface
import space.active.taskmanager1c.domain.use_case.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val updateJobInterface: UpdateJobInterface,
    private val userSettings: GetUserSettingsFromDataStore,
    private val saveUserSettings: SaveUserSettingsToDataStore,
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

    /**
     *
     * Variable for stoppable job witch regular update data after user login
     */
    private var updateJob: Job? = null
    private val runningJob: AtomicBoolean = AtomicBoolean(false)

    fun clearAndExit() {
        viewModelScope.launch {

            val current = userSettings().first()
            saveUserSettings(
                current.copy(username = null, userId = null, password = null)
            ).collect()

            stopUpdateJob()

            clearAllTables().collectLatest {
                if (it) {
                    _exitEvent.emit(true)
                }
            }
        }
    }

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
                        logger.log(TAG, "updateJob start")
                        /**
                        set update work here
                         */
                        updateJobInterface.updateJob(userSettings().first(), 1000L)
                            .catch { e ->
                                exceptionHandler(e)
                                //TODO add error counter and pause
                                delay(5000)
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
            logger.log(TAG, "updateJob is Active: ${updateJob?.isActive}")
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