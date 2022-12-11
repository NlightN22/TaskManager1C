package space.active.taskmanager1c.presentation.screens.task_detailed

data class TaskDetailedTaskState(
    val id: String = "",
    val title: String = "",
    val startDate: String = "",
    val number: String = "",
    val author: String = "",
    val deadLine: String = "",
    val daysEnd: String = "",
    val performer: String = "",
    val coPerfomers: String = "",
    val observers: String = "",
    val description: String = "",
    val taskObject: String = "",
    val mainTask: String = "",
    val innerTasks: String = "",
)

sealed class TaskUserIs {
    data class Author(
        val fields: EditableFields = EditableFields(
            title = true,
            deadLine = true,
            performer = true,
            coPerfomers = true,
            observers = true,
            description = true,
            bottomPerformer = true
        )
    ) : TaskUserIs()

    data class NotAuthorOrPerformer(
        val fields: EditableFields = EditableFields()
    ) : TaskUserIs()

    class Performer(
        val fields: EditableFields = EditableFields(
            bottomPerformer = true
        )
    ) : TaskUserIs()

}

data class TaskIsChanged(
    val bottomShowSave: Boolean = false,
)

data class TaskDetailedExpandState(
    val main: Boolean = false,
    val description: Boolean = false
)

data class TaskDetailedInputMessage(
    val input: String = ""
)