package space.active.taskmanager1c.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.use_case.GetUserSettingsFromDataStore
import space.active.taskmanager1c.domain.use_case.ValidateCredentials
import javax.inject.Inject

private const val TAG = "BaseViewModel"

abstract class BaseViewModel (
    val userSettings: GetUserSettingsFromDataStore,
    val logger: Logger
): ViewModel() {


    override fun onCleared() {
        super.onCleared()
        clearsViewModelScope()
    }

    fun <T> MutableSharedFlow<T>.emitInScope(value: T) {
        viewModelScope.launch {
            this@emitInScope.emit(value)
        }
    }

    fun <T> Flow<T>.collectInScope(block: suspend (T) -> Unit) {
        viewModelScope.launch {
            this@collectInScope.collect {
                block(it)
            }
        }
    }

    private fun clearsViewModelScope() {
        viewModelScope.cancel()
    }
}