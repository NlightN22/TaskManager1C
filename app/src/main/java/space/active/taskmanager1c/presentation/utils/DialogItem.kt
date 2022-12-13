package space.active.taskmanager1c.presentation.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DialogItem(
    val id: String,
    val text: String,
    var checked: Boolean
) : Parcelable {
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