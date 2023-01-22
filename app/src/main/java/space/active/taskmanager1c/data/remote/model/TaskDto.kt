package space.active.taskmanager1c.data.remote.model

import space.active.taskmanager1c.coreutils.TaskHasNotCorrectState
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.CoPerformersInTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.ObserversInTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInputHandled
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskInputHandledWithUsers

data class TaskDto(
    val authorId: String,
    val coPerformers: List<String>,
    val date: String,
    val description: String,
    val endDate: String,
    val id: String,
    val mainTaskId: String,
    val name: String,
    val number: String,
    val objName: String,
    val observers: List<String>,
    val performerId: String,
    val priority: String,
    val status: String
) {

    fun toTaskInputHandledWithUsers(myId: String): TaskInputHandledWithUsers =
        TaskInputHandledWithUsers(
            taskInput = toTaskInputHandled(myId),
            coPerformers = coPerformers.map { it.toCoPerformer() },
            observers = observers.map { it.toObservers() }
        )

    private fun String.toCoPerformer(): CoPerformersInTask = CoPerformersInTask(
        coPerformerId = this,
        taskId = id
    )

    private fun String.toObservers(): ObserversInTask = ObserversInTask(
        observerId = this,
        taskId = id
    )

    private fun toTaskInputHandled(myId: String): TaskInputHandled {
        val author = authorId == myId
        val performer = definePerformer(myId)
        return TaskInputHandled(
            date = date,
            description = description ?: "",
            endDate = endDate ?: "",
            id = id,
            mainTaskId = mainTaskId ?: "",
            name = name ?: "",
            number = number ?: "",
            objName = objName ?: "",
            priority = priority,
            status = status,
            authorId = authorId,
            performerId = performerId,
            isAuthor = author,
            isPerformer = performer,
//        unread =, //not need it
            ok = defineOk(author, performer, status),
            cancel = defineCancel(author, status),
        )
    }

    private fun definePerformer(myId: String): Boolean {
        if (performerId == myId) return true
        if (coPerformers.contains(myId)) return true
        return false
    }

    private fun defineCancel(author: Boolean, status: String): Boolean {
        // author in Status Reviewed
        val curStatus = status.toTaskStatus()
        return author && curStatus == Status.Reviewed
    }

    private fun defineOk(author: Boolean, performer: Boolean, status: String): Boolean {
        val curStatus = status.toTaskStatus()
        // author in Status: New, Accepted, Performed, Reviewed, Deferred
        val authorStatus: Boolean = curStatus != Status.Finished
        // performer in Status: New, Accepted, Performed, Deferred
        val performerStatus: Boolean =
            Status.New == curStatus ||
                    Status.Accepted == curStatus ||
                    Status.Performed == curStatus ||
                    Status.Deferred == curStatus
        if (author) {
            return authorStatus
        }
        if (performer) {
            return performerStatus
        }
        return false
    }

    private fun String.toTaskStatus(): Status {
        return when (this) {
            "new" -> Status.New
            "accepted" -> Status.Accepted
            "performed" -> Status.Performed
            "reviewed" -> Status.Reviewed
            "finished" -> Status.Finished
            "deferred" -> Status.Deferred
            "cancelled" -> Status.Cancelled
            else -> throw TaskHasNotCorrectState
        }
    }

    private enum class Status {
        New,
        Accepted,
        Performed,
        Reviewed,
        Finished,
        Deferred,
        Cancelled
    }
}
