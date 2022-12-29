package space.active.taskmanager1c.presentation.screens.task_detailed

data class EditableFields(
    val title: Boolean = false,
    val deadLine: Boolean = false,
    val performer: Boolean = false,
    val coPerfomers: Boolean = false,
    val observers: Boolean = false,
    val description: Boolean = false,
    val bottomNew: Boolean = false,
    val bottomOk: Boolean = false,
    val bottomCancel: Boolean = false
)