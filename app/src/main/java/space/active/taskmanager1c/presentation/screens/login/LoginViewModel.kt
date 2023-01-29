package space.active.taskmanager1c.presentation.screens.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.UserDomain
import space.active.taskmanager1c.domain.repository.Authorization
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.LoadFromAsset
import space.active.taskmanager1c.domain.use_case.ValidateCredentials
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import javax.inject.Inject

private const val TAG = "LoginViewModel"

@HiltViewModel
class LoginViewModel @Inject constructor(
    settings: SettingsRepository,
    private val loadFromAsset: LoadFromAsset,
    private val authorization: Authorization,
    private val exceptionHandler: ExceptionHandler,
    logger: Logger
) : BaseViewModel(settings, logger) {

    private val _viewState = MutableStateFlow(LoginViewState())
    val viewState = _viewState.asStateFlow()

    private val _authState = MutableStateFlow<StateProgress<Boolean>>(OnWait())
    val authState = _authState.asStateFlow()

    private val validate = ValidateCredentials()

    init {
        logger.log(TAG, "start LoginViewModel")
        viewModelScope.launch {
            val serverAddress = try {
                settings.getServerAddress()
            } catch (e: EmptyObject) {
                null
            }

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
        return withContext(viewModelScope.coroutineContext) {
            try {
                val cred = settings.getCredentials().first()
                val name = cred.username.getString()
                val pass = cred.password.getString()
                Pair(name, pass)
            } catch (e: EmptyObject) {
                Pair(null, null)
            }
        }
    }

    private fun updateUI(username: String, password: String) {
        _viewState.value = _viewState.value.copy(username = username, password = password)
    }

    private suspend fun tryToLoadServerAddress() {
        val addressAsset: String? = loadFromAsset.invoke()
        addressAsset?.let {
            if (validate.server(it)) {
                settings.saveServerAddress(it).collect {}
            }
        }
    }

    fun auth(
        name: String,
        pass: String,
    ) {
        if (!formValidation(name)) return

        // authorization
        viewModelScope.launch {
            updateUI(name, pass)
            try {
                settings.getServerAddress()?.let {
                    if (validate.server(it)) {
                        tryToAuth(name, pass, it)
                    } else {
                        _authState.value = OnWait()
                        exceptionHandler(NotCorrectServerAddress)
                    }
                }
            } catch (e: EmptyObject) {
                exceptionHandler(e)
            }
        }
    }

    private fun formValidation(name: String): Boolean {
        //validation
        if (!validate.userName(name)) {
            _viewState.value =
                _viewState.value.copy(userError = UiText.Resource(R.string.username_valid_error))
            return false
        }

        if (!validate.userName(name)) {
            _viewState.value =
                _viewState.value.copy(passError = UiText.Resource(R.string.password_valid_error))
            return false
        }
        _viewState.value = _viewState.value.copy(userError = null, passError = null)
        return true
    }

    private suspend fun tryToAuth(name: String, pass: String, serverAddress: String) {
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
                        tryToSaveSettings(
                            name = name,
                            userId = request.data.userId,
                            pass = pass,
                            serverAddress = serverAddress
                        )
                    }
                }
            }
    }

    private fun tryToSaveSettings(
        name: String,
        userId: String,
        pass: String,
        serverAddress: String
    ) {
        viewModelScope.launch {
            var exceptions = false
            settings.saveUser(UserDomain(id = userId, name = name))
                .catch {
                    exceptionHandler(it)
                    exceptions = true
                }
                .collect { }
            settings.saveServerAddress(serverAddress)
                .catch {
                    exceptionHandler(it)
                    exceptions = true
                }
                .collect {}
            settings.savePassword(pass)
                .catch {
                    exceptionHandler(it)
                    exceptions = true
                }
                .collect {}
            if (exceptions) {
                _authState.value = OnWait()
                return@launch
            }
            logger.log(TAG, "tryToSaveSettings exceptions: $exceptions")
            _authState.value = Success(true)
        }
    }
}