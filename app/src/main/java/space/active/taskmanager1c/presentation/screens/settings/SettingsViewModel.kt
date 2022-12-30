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
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettings: GetUserSettingsFromDataStore,
    private val saveSettings: SaveUserSettingsToDataStore,
    private val exceptionHandler: ExceptionHandler
) : ViewModel() {
    private val _viewState = MutableStateFlow(SettingsViewState())
    val viewState = _viewState.asStateFlow()


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
            // validation
            val res = Patterns.WEB_URL.matcher(serverAddress).matches()
            if (!res) {
                _viewState.value =
                    _viewState.value.copy(addressError = NotCorrectServerAddress.text)
                return@launch
            }
            // save address
            val changeAddress = curSettings.copy(serverAddress = serverAddress)
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