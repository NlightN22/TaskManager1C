package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.presentation.utils.MultiChooseDialog

data class User(
    val id: String,
    val name: String
) {
    fun toDialogItem(checked: Boolean): MultiChooseDialog.DialogItem {
        return MultiChooseDialog.DialogItem(id = this.id, text = name, checked = checked)
    }

    companion object {
        fun List<User>.toDialogItems(currentSelectedUsersId: List<String>): List<MultiChooseDialog.DialogItem> {
            return this.map {
                if (currentSelectedUsersId.contains(it.id)) {
                    it.toDialogItem(true)
                } else {
                    it.toDialogItem(false)
                }
            }
        }
    }
}
