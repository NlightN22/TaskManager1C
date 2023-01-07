package space.active.taskmanager1c.presentation.screens.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.coreutils.EncryptedData
import space.active.taskmanager1c.coreutils.EncryptedData.Companion.toEncryptedData
import space.active.taskmanager1c.coreutils.NotCorrectServerAddress
import space.active.taskmanager1c.coreutils.SuccessRequest
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
    private val exceptionHandler: ExceptionHandler
) : BaseViewModel(settings, logger) {

    private val _viewState = MutableStateFlow(SettingsViewState())
    val viewState = _viewState.asStateFlow()

    val validateCredentials = ValidateCredentials()


    private val _saveEvent = MutableSharedFlow<Boolean>()
    val saveEvent = _saveEvent.asSharedFlow()

    init {
        // set user
        viewModelScope.launch {
            val user = wrapGetSettings {settings.getUser().name}
            val id = wrapGetSettings { settings.getUser().id }
            val server = wrapGetSettings { settings.getServerAddress() }

            _viewState.value = SettingsViewState(
                userId = id,
                userName = user,
                serverAddress = server
            )
        }
    }

    private suspend fun wrapGetSettings(block: suspend () -> String): String {
        return try {
            block()
        } catch (e:EmptyObject) {
            ""
        }
    }

    fun saveSettings(serverAddress: String) {
        viewModelScope.launch {
            // validation
            val validateRes = validateCredentials.server(serverAddress)

            if (!validateRes) {
                exceptionHandler(NotCorrectServerAddress)
                return@launch
            }
            // save to Settings
            settings.saveServerAddress(serverAddress)
                .catch {
                    exceptionHandler(it)
                }
                .collect {
                    if (it is SuccessRequest) {
                        _saveEvent.emit(true)
                    }
                }
        }
    }
}