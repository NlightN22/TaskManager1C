package space.active.taskmanager1c.presentation.screens.task_detailed

import space.active.taskmanager1c.coreutils.UiText


data class TaskDetailedErrorState (
    val title: UiText? = null,
    val endDate: UiText? = null,
    val author: UiText? = null,
    val performer: UiText? = null,
)