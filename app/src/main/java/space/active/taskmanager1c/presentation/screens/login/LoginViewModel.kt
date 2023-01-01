package space.active.taskmanager1c.presentation.screens.login

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.repository.Authorization
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.GetUserSettingsFromDataStore
import space.active.taskmanager1c.domain.use_case.LoadFromAsset
import space.active.taskmanager1c.domain.use_case.SaveUserSettingsToDataStore
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import javax.inject.Inject

private const val TAG = "LoginViewModel"

@HiltViewModel
class LoginViewModel @Inject constructor(
    userSettings: GetUserSettingsFromDataStore,
    private val saveUserSettings: SaveUserSettingsToDataStore,
    private val loadFromAsset: LoadFromAsset,
    private val authorization: Authorization,
    private val exceptionHandler: ExceptionHandler,
    logger: Logger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel(userSettings, logger) {

    private val _viewState = MutableStateFlow(LoginViewState())
    val viewState = _viewState.asStateFlow()

    private val _authState = MutableStateFlow<StateProgress<Boolean>>(OnWait())
    val authState = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            val serverAddress = userSettings().first().serverAddress

            // read from settings
            val (name, pass) = readFromSettings()

            if (serverAddress == null) {
                tryToLoadServerAddress()
            }

            if (name == null || pass == null) {
                // if null load base URL from asset
                _authState.value = OnWait()
            } else {
                updateUI(name, pass)
                _authState.value = Loading()
                auth(name!!, pass!!)
            }
        }
    }

    private suspend fun readFromSettings(): Pair<String?, String?> {
        val settings = userSettings().first()
        val name = settings.username
        val pass = settings.password
        return Pair(name, pass)
    }

    private fun updateUI(username: String, password: String) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(username = username, password = password)
        }
    }

    private fun validateServerAddress(address: String): Boolean =
        Patterns.WEB_URL.matcher(address).matches()

    private suspend fun tryToLoadServerAddress() {
        val addressAsset: String? = loadFromAsset.invoke()
        addressAsset?.let {
            if (validateServerAddress(addressAsset)) {
                val current = userSettings().first()
                val changed = current.copy(serverAddress = addressAsset)
                saveUserSettings(changed).collect {
                    when (it) {
                        is ErrorRequest -> {
                            exceptionHandler(it.exception)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun auth(
        name: String,
        pass: String,
    ) {
        viewModelScope.launch {
            updateUI(name, pass)
            val serverAddress = userSettings().first().serverAddress
            serverAddress?.let {
                if (validateServerAddress(it)) {
                    tryToAuth(name, pass, it)
                } else {
                    _authState.value = OnWait()
                    exceptionHandler(NotCorrectServerAddress)
                }
            } ?: kotlin.run {
                _authState.value = OnWait()
                exceptionHandler(NotCorrectServerAddress)
            }
        }
    }

    private suspend fun tryToAuth(name: String, pass: String, serverAddress: String) {
        // todo send address to retrofit module
        authorization.auth(name, pass)
            .catch {
                exceptionHandler(it)
                _authState.value = OnWait()
            }
            .collect { request ->
                when (request) {
                    is PendingRequest -> {
                        _authState.value = Loading()
                    }
                    is ErrorRequest -> {
                        _authState.value = OnWait()
                        exceptionHandler(request.exception)
                    }
                    is SuccessRequest -> {
                        tryToSaveSettings(name, request.data.userId, pass, serverAddress)
                    }
                }
            }
    }

    private suspend fun tryToSaveSettings(
        name: String,
        userId: String,
        pass: String,
        serverAddress: String
    ) {
        val changed = userSettings().first().copy(
            username = name,
            userId = userId,
            password = pass,
            serverAddress = serverAddress
        )
        saveUserSettings(changed).collect { saveRequest ->
            when (saveRequest) {
                is ErrorRequest -> {
                    _authState.value = OnWait()
                    exceptionHandler(saveRequest.exception)
                }
                is SuccessRequest -> {
                    _authState.value = Success(true)
                }
                is PendingRequest -> {}
            }
        }
    }
}