package space.active.taskmanager1c.domain.models

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.coreutils.toShortDate
import space.active.taskmanager1c.domain.repository.TasksRepository

data class TaskTitleViewState(
    val title: String = "",
    val date: String = "",
    val number: String = "",
    val status: Int? = null,
) {
    companion object {
        suspend fun MutableStateFlow<TaskTitleViewState>.setTitleState(
            tasksRepository: TasksRepository,
            taskId: String
        ) {
            tasksRepository.getTask(taskId).collectLatest { task ->
                task?.let {
                    this.value = this.value.copy(
                        title = it.name,
                        date = it.date.toShortDate(),
                        number = it.number,
                        status = it.status.getResId()
                    )
                }
            }
        }
    }
}