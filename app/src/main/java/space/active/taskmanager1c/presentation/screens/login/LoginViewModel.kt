package space.active.taskmanager1c.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.UserSettings
import space.active.taskmanager1c.domain.repository.Authorization
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.GetUserSettingsFromDataStore
import space.active.taskmanager1c.domain.use_case.SaveUserSettingsToDataStore
import javax.inject.Inject

private const val TAG = "LoginViewModel"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userSettings: GetUserSettingsFromDataStore,
    private val saveUserSettings: SaveUserSettingsToDataStore,
    private val tasksRepository: TasksRepository,
    private val authorization: Authorization,
    private val exceptionHandler: ExceptionHandler,
    private val logger: Logger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _authState = MutableStateFlow<StateProgress<String>>(OnWait())
    val authState = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            userSettings().collectLatest { settings ->
                val name = settings.username
                val pass = settings.password
                if (name == null || pass == null) {
                    _authState.value = OnWait()
                } else {
                    _authState.value = Loading()
                    auth(name, pass)
                }
            }
        }
    }

    fun auth(name: String, pass: String) {
        viewModelScope.launch {
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
                        saveUserToStore(
                            userName = request.data.userName,
                            userId = request.data.userId, // todo get userid from server response
                            userPass = pass
                        )
                        _authState.value = Success("")
                    }
                }
            }
        }
    }

    private fun saveUserToStore(userName: String, userId: String, userPass: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                logger.log(TAG, "saveUserToStore $userName $userId $userPass")
                saveUserSettings(
                    UserSettings(
                        userName,
                        userId,
                        userPass
                    )
                )
            } catch (e: Exception) {
                exceptionHandler(e)
            }
        }
    }
}