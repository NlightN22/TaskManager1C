package space.active.taskmanager1c.presentation.screens.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import space.active.taskmanager1c.coreutils.EncryptedData
import space.active.taskmanager1c.coreutils.EncryptedData.Companion.toEncryptedData
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
            _viewState.value = SettingsViewState(
                userId = settings.getUser()?.id ?: "",
                userName = settings.getUser()?.name ?: "",
                serverAddress = settings.getServerAddress() ?: ""
            )
        }
    }

    fun saveSettings(serverAddress: String) {
        // validation
        val validateRes = validateCredentials.server(serverAddress)

        if (!validateRes) {
            exceptionHandler(NotCorrectServerAddress)
            return
        }
        // save to Settings
        settings.saveServerAddress(serverAddress)
    }

    // todo delete
//    fun saveMock(serverAddress: String) {
//        val curSettings = UserSettings(
//            username = _viewState.value.userName,
//            userId = _viewState.value.userId,
//            serverAddress = serverAddress
//        )
//        encrTmp(serverAddress)
//    }

    private fun encrTmp(serverAddress: String) {
        val encrypted: EncryptedData? = serverAddress.toEncryptedData()
        logger.log(TAG, "encrypted: ${encrypted}")
        encrypted?.let {
            val json = Json.encodeToString(EncryptedData.serializer(), it)
            logger.log(TAG, "serialized: $json")
            val deSerial = Json.decodeFromString(EncryptedData.serializer(), json)
            logger.log(TAG, "deserialized: $deSerial")
            val decrypted: String? = deSerial.getString()
            logger.log(TAG, "decrypted: $decrypted")
        }
    }

}