package space.active.taskmanager1c.presentation.screens.task_detailed

import space.active.taskmanager1c.domain.models.ClickableTask
import space.active.taskmanager1c.domain.models.TaskDomain
import space.active.taskmanager1c.domain.models.TaskTitleViewState
import space.active.taskmanager1c.presentation.utils.dialogs.DialogItem
import space.active.taskmanager1c.presentation.utils.dialogs.EditTextDialogStates

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
    val mainTask: ClickableTask = ClickableTask(),
    val innerTasks: List<ClickableTask> = emptyList(),
    val status: TaskDomain.Status = TaskDomain.Status.New,
) {
    fun toTaskTitleViewState() = TaskTitleViewState(
        title = title,
        date = startDate,
        number = number,
        status = status.getResId()
    )

    fun List<ClickableTask>.toText(): String {
        return this.map { it.name }.joinToString("\n")
    }
}

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

data class InnerTasksDialog(
    val listTasks: List<ClickableTask>?
) : TaskDetailedDialogs()