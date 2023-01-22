package space.active.taskmanager1c.presentation.screens.task_detailed

import space.active.taskmanager1c.domain.models.TaskDomain
import space.active.taskmanager1c.presentation.utils.DialogItem
import space.active.taskmanager1c.presentation.utils.EditTextDialogStates

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
    val status: TaskDomain.Status = TaskDomain.Status.New,
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