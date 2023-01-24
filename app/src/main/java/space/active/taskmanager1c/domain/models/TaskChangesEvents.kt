package space.active.taskmanager1c.domain.models

import java.time.ZonedDateTime

sealed class TaskChangesEvents {

    data class Title(
        val title: String
    ) : TaskChangesEvents()

    data class EndDate(
        val date: ZonedDateTime
    ) : TaskChangesEvents()

    data class Performer(
        val userDomain: UserDomain
    ) : TaskChangesEvents()

    data class CoPerformers(
        val userDomains: List<UserDomain>
    ) : TaskChangesEvents()

    data class Observers(
        val userDomains: List<UserDomain>
    ) : TaskChangesEvents()

    data class Description(
        val text: String
    ) : TaskChangesEvents()

    data class Status(
        val status: Boolean
    ) : TaskChangesEvents() {
    }
}