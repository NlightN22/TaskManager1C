package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.data.local.db.tasks_room_db.SortField
import space.active.taskmanager1c.data.local.db.tasks_room_db.SortType

sealed class TaskListOrderTypes {
    data class Name(
        val desc: Boolean = false
    ) : TaskListOrderTypes()

    data class Performer(
        val desc: Boolean = false
    ) : TaskListOrderTypes()

    data class StartDate(
        val desc: Boolean = false
    ) : TaskListOrderTypes()

    data class EndDate(
        val desc: Boolean = false
    ) : TaskListOrderTypes()

    fun getSortFieldAndType(): Pair<SortField, SortType> {
        return when (this) {
            is Name -> {
                if (this.desc) {
                    SortField.NAME to SortType.DESCENDING
                } else {
                    SortField.NAME to SortType.ASCENDING
                }
            }
        is Performer -> {
            if (this.desc) {
                SortField.PERFORMER to SortType.DESCENDING
            } else {
                SortField.PERFORMER to SortType.ASCENDING
            }
        }
        is StartDate -> {
            if (this.desc) {
                SortField.DATE to SortType.DESCENDING
            } else {
                SortField.DATE to SortType.ASCENDING
            }
        }
        is EndDate -> {
            if (this.desc) {
                SortField.END_DATE to SortType.DESCENDING
            } else {
                SortField.END_DATE to SortType.ASCENDING
            }
        }
    }
}
}