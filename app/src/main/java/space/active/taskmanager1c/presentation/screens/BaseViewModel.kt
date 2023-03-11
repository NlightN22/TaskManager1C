package space.active.taskmanager1c.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.coreutils.toShortDate
import space.active.taskmanager1c.domain.models.TaskTitleViewState
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.repository.TasksRepository

private const val TAG = "BaseViewModel"

abstract class BaseViewModel(
    val settings: SettingsRepository,
    val logger: Logger
) : ViewModel() {

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