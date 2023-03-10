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
            bottomMessage = true,
            bottomAttach = true
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
            bottomCancel = true,
            bottomMessage = true,
            bottomAttach = true
        )
    ) : TaskUserIs(fields)

    data class AuthorInNewTask(
        override val fields: EditableFields = Author().fields.copy(bottomNew = true)
    ) : TaskUserIs(fields)

    data class NotAuthorOrPerformer(
        override val fields: EditableFields = EditableFields(
            bottomMessage = true,
            bottomAttach = true
        )
    ) : TaskUserIs(fields)

    data class Performer(
        override val fields: EditableFields = EditableFields(
            bottomOk = true,
            bottomMessage = true,
            bottomAttach = true
        )
    ) : TaskUserIs(fields)

    data class PerformerInReviewed(
        override val fields: EditableFields = EditableFields(
            bottomCancel = true,
            bottomMessage = true,
            bottomAttach = true
        )
    ) : TaskUserIs(fields)

    object Observer : TaskUserIs()
}
