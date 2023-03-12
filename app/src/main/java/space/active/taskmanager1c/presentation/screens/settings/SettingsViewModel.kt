package space.active.taskmanager1c.presentation.screens.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.coreutils.NotCorrectServerAddress
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.ValidateCredentials
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import javax.inject.Inject

private const val TAG = "SettingsViewModel"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    settings: SettingsRepository,
    logger: Logger,
    private val exceptionHandler: ExceptionHandler,
    private val retrofit: Retrofit
) : BaseViewModel(settings, logger) {

    private val _viewState = MutableStateFlow(SettingsViewState())
    val viewState = _viewState.asStateFlow()

    private val _visibleState = MutableStateFlow(SettingsVisibleState())
    val visibleState = _visibleState.asStateFlow()

    private val _saveEvent = MutableSharedFlow<Boolean>()
    val saveEvent = _saveEvent.asSharedFlow()

    fun setSettingsViewState(loginState: Boolean) {
        _visibleState.value = SettingsVisibleState.getIsLogin(loginState)

        // set userDomain
        viewModelScope.launch {
            val user = wrapGetSettings { settings.getUser().name }
            val id = wrapGetSettings { settings.getUser().id }
            val server: String = retrofit.baseUrl().toString()
            _viewState.value = _viewState.value.copy(
                userId = id,
                userName = user,
                serverAddress = server
            )
            // only for settings saved after login
            if (loginState) {
                _viewState.value =
                    _viewState.value.copy(skipStatusAlert = settings.getSkipStatusAlert())
            }
        }
    }

    var changeJob: Job? = null
    fun changeServerAddressState(text: String) {
        changeJob?.cancel()
        changeJob = viewModelScope.launch {
            delay(100)
            _viewState.value = _viewState.value.copy(serverAddress = text)
        }
    }

    fun changeStatusAlert(value: Boolean) {
        logger.log(TAG, "skipStatusAlert: $value")
        _viewState.value = _viewState.value.copy(skipStatusAlert = value)
    }

    fun saveSettings() {
        viewModelScope.launch {
            wrapSaveExceptions {
                settings.saveSkipStatusAlert(_viewState.value.skipStatusAlert)
                _saveEvent.emit(true)
            }
        }
    }

    private suspend fun <T> wrapSaveExceptions(block: suspend () -> T) {
        try {
            block()
        } catch (e: Exception) {
            exceptionHandler(e)
        }
    }

    private suspend fun wrapGetSettings(block: suspend () -> String): String {
        return try {
            block()
        } catch (e: EmptyObject) {
            ""
        }
    }
}