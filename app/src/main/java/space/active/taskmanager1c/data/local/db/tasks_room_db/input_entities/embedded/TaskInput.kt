package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded

import androidx.room.PrimaryKey
import space.active.taskmanager1c.coreutils.TaskHasNotCorrectState
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInputHandled
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput

    // todo delete
//data class TaskInput(
//    val date: String,
//    val description: String,
//    val endDate: String?,
//    @PrimaryKey(autoGenerate = false)
//    val id: String,
//    val mainTaskId: String?,
//    val name: String,
//    val number: String,
//    val objName: String?,
//    val priority: String,
//    val status: String,
//    val authorId: String,
//    val performerId: String,
////    val usersInTask: UsersInTask
//) {
//
//    fun toTaskInputHandled(userDomain: UserInput): TaskInputHandled {
//        val author = this.authorId == userDomain.id
//        val performer = definePerformer(userDomain)
//        val status = this.status.toTaskStatus()
//        return TaskInputHandled(
//            taskIn = this,
//            whoIsInTask = WhoIsInTask(
//                author, performer
//            ),
//            ok = defineOk(author, performer, status),
//            cancel = defineCancel(author, status)
//        )
//    }
//
//    private fun definePerformer(user: UserInput): Boolean {
//        var inList = if (this.usersInTask.coPerformers.isNullOrEmpty()) {
//            false
//        } else {
//            this.usersInTask.coPerformers.any { it == user.id }
//        }
//        return this.usersInTask.performerId == user.id || inList
//    }
//
//    private fun defineCancel(author: Boolean, status: Status): Boolean {
//        // author in Status Reviewed
//        return author && status == Status.Reviewed
//    }
//
//    private fun defineOk(author: Boolean, performer: Boolean, status: Status): Boolean {
//        // author in Status: New, Accepted, Performed, Reviewed, Deferred
//        val authorStatus: Boolean = status != Status.Finished
//        // performer in Status: New, Accepted, Performed, Deferred
//        val performerStatus: Boolean =
//            Status.New == status ||
//                    Status.Accepted == status ||
//                    Status.Performed == status ||
//                    Status.Deferred == status
//        if (author) {
//            return authorStatus
//        }
//        if (performer) {
//            return performerStatus
//        }
//        return false
//    }
//
//    private fun String.toTaskStatus(): Status {
//        return when (this) {
//            "new" -> Status.New
//            "accepted" -> Status.Accepted
//            "performed" -> Status.Performed
//            "reviewed" -> Status.Reviewed
//            "finished" -> Status.Finished
//            "deferred" -> Status.Deferred
//            "cancelled" -> Status.Cancelled
//            else -> throw TaskHasNotCorrectState
//        }
//    }
//
//    private enum class Status {
//        New,
//        Accepted,
//        Performed,
//        Reviewed,
//        Finished,
//        Deferred,
//        Cancelled
//    }
//}
