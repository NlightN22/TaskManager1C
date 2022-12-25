package space.active.taskmanager1c.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.repository.Authorization
import space.active.taskmanager1c.domain.repository.DataStoreRepository
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import javax.inject.Inject

private const val TAG = "LoginViewModel"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val authorization: Authorization,
    private val exceptionHandler: ExceptionHandler,
    private val logger: Logger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _authState = MutableStateFlow<StateProgress<String>>(OnWait())
    val authState = _authState.asStateFlow()

    private val _userName = dataStoreRepository.getUserName().catch { exceptionHandler(it) }
    private val _userPass = dataStoreRepository.getUserPass().catch { exceptionHandler(it) }

    private val _credentials: Flow<Pair<String, String>> =
        combine(_userName, _userPass) { name, pass ->
            if (name != null && pass != null) {
                Pair(name, pass)
            } else {
                Pair("", "")
            }
        }

    init {
        viewModelScope.launch {
            _credentials.collect { cred ->
                val name = cred.first
                val pass = cred.second
                logger.log(TAG, "name $name pass $pass")
                if (name.isBlank() || pass.isBlank()) {
                    _authState.value = OnWait()
                } else {
                    _authState.value = Loading()
                    auth(name, pass)
                }
            }
        }
    }

    // check saved datastore if empty show login else auth
    // auth user
    // if success save to datastore
    fun auth(name: String, pass: String) {
        viewModelScope.launch {
            authorization.auth(name, pass).collect { request ->
                when (request) {
                    is PendingRequest -> {
                        _authState.value = Loading()
                    }
                    is ErrorRequest -> {
                        _authState.value = OnWait()
                        exceptionHandler(request.exception)
                    }
                    is SuccessRequest -> {
                        with(request.data) {
                            saveUserToStore(userName = userName, userId = userId, userPass = request.data.pass)
                        }
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
                with(dataStoreRepository) {
                    setUserId(userId)
                    setUserName(userName)
                    setUserPass(userPass)
                }
            } catch (e: Exception) {
                exceptionHandler(e)
            }
        }
    }
}