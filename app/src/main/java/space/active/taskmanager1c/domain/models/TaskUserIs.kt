package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.presentation.screens.task_detailed.EditableFields

sealed class TaskUserIs(
    open val fields: EditableFields = EditableFields()
) {
    data class Author(
        override val fields: EditableFields = EditableFields(
            title = true,
            deadLine = true,
            performer = true,
            coPerfomers = true,
            observers = true,
            description = true,
            bottomOk = true,
        )
    ) : TaskUserIs(fields)

    data class AuthorInReviewed(
        override val fields: EditableFields = EditableFields(
            title = true,
            deadLine = true,
            performer = true,
            coPerfomers = true,
            observers = true,
            description = true,
            bottomOk = true,
            bottomCancel = true
        )
    ) : TaskUserIs(fields)

    data class NotAuthorOrPerformer(
        override val fields: EditableFields = EditableFields()
    ) : TaskUserIs(fields)

    data class Performer(
        override val fields: EditableFields = EditableFields(
            bottomOk = true,
        )
    ) : TaskUserIs(fields)

    data class PerformerInReviewed(
        override val fields: EditableFields = EditableFields(
            bottomCancel = true
        )
    ) : TaskUserIs(fields)

    object Observer : TaskUserIs()

    companion object {
        fun userIs(task: Task, whoAmI: User): TaskUserIs {
            if (task.users.author == whoAmI) {
                if (task.status == Task.Status.Reviewed) {
                    return AuthorInReviewed()
                }
                return Author()
            } else if (task.users.performer == whoAmI || task.users.coPerformers.contains(whoAmI)) {
                //if performer in reviewed can only resume task
                if (task.status == Task.Status.Reviewed) {
                    return PerformerInReviewed()
                }
                return Performer()
            } else if (task.users.observers.contains(whoAmI)) {
                return Observer
            }
            return NotAuthorOrPerformer()
        }
    }
}
