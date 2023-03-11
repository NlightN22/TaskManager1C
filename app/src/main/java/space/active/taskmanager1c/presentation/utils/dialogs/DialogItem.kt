package space.active.taskmanager1c.presentation.utils.dialogs

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import space.active.taskmanager1c.domain.models.ClickableTask

@Parcelize
data class DialogItem(
    val id: String,
    val text: String,
    var checked: Boolean
) : Parcelable {

    fun toClickableTask() = ClickableTask(
        name = text,
        id = id
    )

    companion object {
        fun List<DialogItem>.toggleDialogItem(item: DialogItem): List<DialogItem> {
            return this.map {
                if (it.id == item.id) {
                    it.copy(checked = !item.checked)
                } else {
                    it
                }
            }
        }
    }
}