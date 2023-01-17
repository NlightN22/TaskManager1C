package space.active.taskmanager1c.presentation.screens.task_detailed

import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.presentation.utils.DialogItem
import space.active.taskmanager1c.presentation.utils.EditTextDialogStates
import java.time.LocalDate

sealed class TaskDetailedViewState(
    open val state: TaskState
) {
    data class New(
        override val state: TaskState = TaskState()
    ) : TaskDetailedViewState(state) {
        fun setNew(author: String): TaskDetailedViewState.New {
            return this.copy(
                state = state.copy(
                    author = author,
                    startDate = LocalDate.now().toString(),
                    deadLine = LocalDate.now().toString(),
                    status = Task.Status.New
                )
            )
        }
    }

    data class Edit(
        override val state: TaskState
    ) : TaskDetailedViewState(state)
}

data class TaskState(
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
    val status: Task.Status = Task.Status.New,
)

data class TaskDetailedExpandState(
    val main: Boolean = false,
    val description: Boolean = false
)

sealed class TaskDetailedDialogs
data class CoPerformersDialog(
    val listDialogItems: List<DialogItem>?
) : TaskDetailedDialogs()
data class ObserversDialog(
    val listDialogItems: List<DialogItem>?
) : TaskDetailedDialogs()
data class PerformerDialog(
    val listUsers: List<DialogItem>?
) : TaskDetailedDialogs()
object DatePicker : TaskDetailedDialogs()
data class EditTitleDialog(
    val dialogState: EditTextDialogStates?,
) : TaskDetailedDialogs()
data class EditDescriptionDialog(
    val dialogState: EditTextDialogStates?,
) : TaskDetailedDialogs()