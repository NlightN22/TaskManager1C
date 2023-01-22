package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.CoPerformersInTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.ObserversInTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.presentation.utils.DialogItem

data class UserDomain(
    val id: String,
    val name: String
) {

    fun toCoPerformer(taskId: String) = CoPerformersInTask(
        coPerformerId = id,
        taskId = taskId
    )

    fun toObservers(taskId: String) = ObserversInTask(
        observerId = id,
        taskId = taskId
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other != null && other is UserDomain) {
            return this.id == other!!.id
        }
        return false
    }

    fun toDialogItem(checked: Boolean): DialogItem {
        return DialogItem(id = this.id, text = name, checked = checked)
    }

    fun toUserInput(): UserInput = UserInput(
        id, name
    )

    companion object {

        fun UserInput.fromUserInput(): UserDomain = UserDomain(
            id = this.userId,
            name = this.userName
        )

        fun blankUser() = UserDomain(id = "", name = "")

        fun List<UserDomain>.toDialogItems(currentSelectedUsersId: List<String>): List<DialogItem> {
            return this.map {
                if (currentSelectedUsersId.contains(it.id)) {
                    it.toDialogItem(true)
                } else {
                    it.toDialogItem(false)
                }
            }
        }

        fun List<DialogItem>?.fromDialogItems(): List<UserDomain> {
            return this?.filter { it.checked }?.map { fromDialogItem(it) }
                ?: emptyList<UserDomain>()
        }

        fun fromDialogItem(dialogItem: DialogItem) = UserDomain(
            id = dialogItem.id,
            name = dialogItem.text
        )

        fun List<UserDomain>.toText(): String {
            if (this.isNotEmpty()) {
                return this.map { it.name }.toString().dropLast(1).drop(1)
            } else {
                return ""
            }
        }
    }
}
