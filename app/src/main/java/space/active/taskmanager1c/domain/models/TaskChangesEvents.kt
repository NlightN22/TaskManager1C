package space.active.taskmanager1c.domain.models

import java.text.SimpleDateFormat
import java.util.*

sealed class TaskChangesEvents {

    data class Title(
        val title: String
    ) : TaskChangesEvents()

    data class EndDate(
        val date: Date
    ) : TaskChangesEvents() {
        fun toTaskDate(): String {
            val formatter = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.getDefault()
            )
            return formatter.format(this.date)
        }
    }

    data class Performer(
        val user: User
    ) : TaskChangesEvents()

    data class CoPerformers(
        val users: List<User>
    ) : TaskChangesEvents()

    data class Observers(
        val users: List<User>
    ) : TaskChangesEvents()

    data class Description(
        val text: String
    ) : TaskChangesEvents()

    data class Status(
        val status: Boolean
    ) : TaskChangesEvents()
}