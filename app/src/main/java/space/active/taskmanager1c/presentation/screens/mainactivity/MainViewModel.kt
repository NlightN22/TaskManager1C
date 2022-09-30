package space.active.taskmanager1c.presentation.screens.mainactivity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import space.active.taskmanager1c.data.local.db.TasksFromRemote.TasksFromRemoteDb
import space.active.taskmanager1c.data.local.db.TasksFromRemote.entity.TaskDb
import space.active.taskmanager1c.domain.utils.Resource
import javax.inject.Inject

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tmpRepo: TasksFromRemoteDb
) : ViewModel() {

    init {
        viewModelScope.launch {
            tmpRepo.updateListener().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.e(TAG, "Success: ${result.data}")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Error data: ${result.data}, message: ${result.message}")
                    }
                    is Resource.Loading -> {
                        Log.e(TAG, "Loading: ${result.data}")
                    }
                }

            }
        }
    }

    val taskList: Flow<List<TaskDb>> = tmpRepo.listTasks.map { it.data ?: emptyList() }

}