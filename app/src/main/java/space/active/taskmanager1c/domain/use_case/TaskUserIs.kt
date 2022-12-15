package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.presentation.screens.task_detailed.EditableFields

sealed class TaskUserIs (
    open val fields: EditableFields
        ) {
    data class Author(
        override val fields: EditableFields = EditableFields(
            title = true,
            deadLine = true,
            performer = true,
            coPerfomers = true,
            observers = true,
            description = true,
            bottomPerformer = true
        )
    ) : TaskUserIs(fields)

    data class NotAuthorOrPerformer(
        override val fields: EditableFields = EditableFields()
    ) : TaskUserIs(fields)

    class Performer(
        override val fields: EditableFields = EditableFields(
            bottomPerformer = false
        )
    ) : TaskUserIs(fields)

    companion object {
        fun userIs(task: Task, whoAmI: User): TaskUserIs {
            if (task.users.author == whoAmI) {
                return Author()
            } else if (task.users.performer == whoAmI && task.users.author != whoAmI) {
                return Performer()
            } else {
                return NotAuthorOrPerformer()
            }
        }
    }
}
