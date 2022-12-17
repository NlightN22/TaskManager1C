package space.active.taskmanager1c.domain.models

sealed class TaskListOrderTypes {
    data class Name(
        val desc: Boolean = false
    ) : TaskListOrderTypes()
    data class Performer(
        val desc: Boolean = false
    ): TaskListOrderTypes()
    data class StartDate(
        val desc: Boolean = false
    ): TaskListOrderTypes()
    data class EndDate(
        val desc: Boolean = false
    ): TaskListOrderTypes()
}