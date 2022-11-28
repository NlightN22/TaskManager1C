package space.active.taskmanager1c.presentation.screens.mainactivity

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.active.taskmanager1c.data.repository.TaskApi
import javax.inject.Inject

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tmpApi: TaskApi
) : ViewModel() {

//    val taskList: Flow<List<TaskInput>> = tmpRepo.listTasks.map { it.data ?: emptyList() }


private fun updateData() {

}



}