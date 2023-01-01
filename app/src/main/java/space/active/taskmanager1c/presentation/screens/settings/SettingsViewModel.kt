package space.active.taskmanager1c.presentation.screens.settings

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.GetUserSettingsFromDataStore
import space.active.taskmanager1c.domain.use_case.SaveUserSettingsToDataStore
import space.active.taskmanager1c.domain.use_case.ValidateCredentials
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettings: GetUserSettingsFromDataStore,
    private val saveSettings: SaveUserSettingsToDataStore,
    private val exceptionHandler: ExceptionHandler
) : ViewModel() {
    private val _viewState = MutableStateFlow(SettingsViewState())
    val viewState = _viewState.asStateFlow()

    val validateCredentials = ValidateCredentials()


    private val _saveEvent = MutableSharedFlow<Boolean>()
    val saveEvent = _saveEvent.asSharedFlow()

    init {
        // set user
        viewModelScope.launch {
            val settings = userSettings().first()
            _viewState.value = SettingsViewState(
                userId = settings.userId ?: "",
                userName = settings.username ?: "",
                serverAddress = settings.serverAddress ?: ""
            )
        }
    }

    fun saveSettings(serverAddress: String) {
        viewModelScope.launch {

            val curSettings = userSettings().first()
            val changeAddress = curSettings.copy(serverAddress = serverAddress)

            // validation
            val validateRes = validateCredentials(changeAddress)

            if (!validateRes) {
                exceptionHandler(NotCorrectServerAddress)
                return@launch
            }
            // save to DataStore
            saveSettings(changeAddress).collect { saveRequest ->
                when (saveRequest) {
                    is ErrorRequest -> {
                        exceptionHandler(saveRequest.exception)
                    }
                    is SuccessRequest -> {
                        _saveEvent.emit(true)
                    }
                    is PendingRequest -> {}
                }
            }
        }
    }
}