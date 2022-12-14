package space.active.taskmanager1c.presentation.screens.task_detailed

import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.User
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

sealed class TaskDetailedChangesEvents

data class ChangeTaskTitle(
    val title: String
) : TaskDetailedChangesEvents()

data class ChangeEndDate(
    val date: Date
) : TaskDetailedChangesEvents() {
    fun toTaskDate(): String {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault()
        )
        return formatter.format(this.date)
    }
}

data class ChangeTaskPerformer(
    val user: User
) : TaskDetailedChangesEvents()

data class ChangeTaskCoPerformers(
    val users: List<User>
) : TaskDetailedChangesEvents()

data class ChangeTaskObservers(
    val users: List<User>
) : TaskDetailedChangesEvents()

data class ChangeTaskDescription(
    val text: String
) : TaskDetailedChangesEvents()

data class ChangeTaskStatus(
    val status: Task.Status
) : TaskDetailedChangesEvents()
