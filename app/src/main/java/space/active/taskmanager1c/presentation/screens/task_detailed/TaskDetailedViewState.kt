package space.active.taskmanager1c.presentation.screens.task_detailed

data class TaskDetailedTaskState(
    val id: String = "",
    val title: String = "",
    val startDate: String = "",
    val number: String = "",
    val author: String = "",
    val deadLine: String = "",
    val performer: String = "",
    val coPerfomers: String = "",
    val observers: String = "",
    val description: String = "",
    val mainTask: String = "",
    val innerTasks: String = "",
)

data class TaskDetailedExpandState(
    val main: Boolean = false,
    val description: Boolean = false
)

data class TaskDetailedInputMessage(
    val input: String = ""
)

data class TaskDetailedSaveChangesState(
    val show: Boolean = false
)
