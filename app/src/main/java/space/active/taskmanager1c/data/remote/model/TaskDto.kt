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

    //todo delete
//    private fun definePerformer(myId: String): Boolean {
//        var inList = if (this.usersInTask.coPerformers.isNullOrEmpty()) {
//            false
//        } else {
//            this.usersInTask.coPerformers.any { it == user.id }
//        }
//        return this.usersInTask.performerId == user.id || inList
//    }

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

    //todo delete
//    companion object {
//        fun fromOutputTask(outputTask: OutputTask): TaskDto = TaskDto(
//            authorId = outputTask.taskHandled.taskInput.authorId,
//            coPerformers = outputTask.taskHandled.usersInTask.coPerformers,
//            date = outputTask.taskHandled.date,
//            description = outputTask.taskHandled.description,
//            endDate = outputTask.taskHandled.endDate ?: "",
//            id = outputTask.taskHandled.id,
//            mainTaskId = outputTask.taskHandled.mainTaskId ?: "",
//            name = outputTask.taskHandled.name,
//            number = outputTask.taskHandled.number,
//            objName = outputTask.taskHandled.objName ?: "",
//            observers = outputTask.taskHandled.usersInTask.observers,
//            performerId = outputTask.taskHandled.usersInTask.performerId,
//            priority = outputTask.taskHandled.priority,
//            status = outputTask.taskHandled.status
//        )
//
//        fun fromInputTask(inputTask: TaskInput): TaskDto = TaskDto(
//            authorId = inputTask.usersInTask.authorId,
//            coPerformers = inputTask.usersInTask.coPerformers,
//            date = inputTask.date,
//            description = inputTask.description,
//            endDate = inputTask.endDate ?: "",
//            id = inputTask.id,
//            mainTaskId = inputTask.mainTaskId ?: "",
//            name = inputTask.name,
//            number = inputTask.number,
//            objName = inputTask.objName ?: "",
//            observers = inputTask.usersInTask.observers,
//            performerId = inputTask.usersInTask.performerId,
//            priority = inputTask.priority,
//            status = inputTask.status
//        )
//    }
}
