package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.presentation.utils.DialogItem

data class User(
    val id: String,
    val name: String
) {
    fun toDialogItem(checked: Boolean): DialogItem {
        return DialogItem(id = this.id, text = name, checked = checked)
    }

    companion object {
        fun List<User>.toDialogItems(currentSelectedUsersId: List<String>): List<DialogItem> {
            return this.map {
                if (currentSelectedUsersId.contains(it.id)) {
                    it.toDialogItem(true)
                } else {
                    it.toDialogItem(false)
                }
            }
        }

        fun List<DialogItem>?.fromDialogItems(): List<User> {
            return this?.filter { it.checked }?.map { fromDialogItem(it) } ?: emptyList<User>()
        }

        fun fromDialogItem(dialogItem: DialogItem) = User(
            id = dialogItem.id,
            name = dialogItem.text
        )

        fun List<User>.toText(): String {
            if (this.isNotEmpty()) {
                return this.map { it.name }.toString().dropLast(1).drop(1)
            } else {
                return ""
            }
        }
    }
}
