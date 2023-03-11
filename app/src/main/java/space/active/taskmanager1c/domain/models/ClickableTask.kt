package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.presentation.utils.dialogs.DialogItem

data class ClickableTask(
    val name: String = "",
    val id: String = ""
) {
    fun toDialogItem() = DialogItem(
        id = id,
        text = name,
        checked = false
    )

    companion object {
        fun List<ClickableTask>.toDialogListItems() = this.map { it.toDialogItem() }
    }
}