package space.active.taskmanager1c.domain.models

import android.util.Log
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.UiText
import java.time.LocalDateTime
import java.util.*

sealed class TaskChangesEvents {

    data class Title(
        val title: String
    ) : TaskChangesEvents()

    data class EndDate(
        val date: LocalDateTime
    ) : TaskChangesEvents()

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
    ) : TaskChangesEvents() {
    }
}