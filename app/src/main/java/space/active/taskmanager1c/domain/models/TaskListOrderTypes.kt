package space.active.taskmanager1c.domain.models

sealed class TaskListOrderTypes {
    object Name : TaskListOrderTypes()
    object ReverseName: TaskListOrderTypes()
    object Performer: TaskListOrderTypes()
    // todo add reverse
    object Author: TaskListOrderTypes()
    // todo add reverse
    object StartDate: TaskListOrderTypes()
    // todo add reverse
    object EndDate: TaskListOrderTypes()
    // todo add reverse
}