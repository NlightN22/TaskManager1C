package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities

import androidx.room.Embedded
import androidx.room.Entity
import space.active.taskmanager1c.coreutils.toDateTime
import space.active.taskmanager1c.coreutils.toDateTimeOrNull
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.WhoIsInTask
import space.active.taskmanager1c.domain.models.TaskDomain
import java.time.format.DateTimeFormatter

@Entity(primaryKeys = ["id"])
data class TaskInputHandled(
    @Embedded val taskIn: TaskInput,
    @Embedded val whoIsInTask: WhoIsInTask,
    val unread: Boolean = false,
    val ok: Boolean = false,
    val cancel: Boolean = false
) {
    fun toTaskDomain(listUsersInput: List<UserInput>): TaskDomain {
        with(this.taskIn) {
            return TaskDomain(
                date = date.toDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                description = description,
                endDate = endDate?.toDateTimeOrNull(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                id = id,
                mainTaskId = mainTaskId ?: "",
                name = name,
                number = number,
                objName = objName ?: "",
                priority = priority,
                status = TaskDomain.toTaskStatus(
                    status
                ),
                users = usersInTask.toUsersDomain(listUsersInput),
                whoIsInTask = whoIsInTask,
                unread = unread,
                ok = ok,
                cancel = cancel
            )
        }
    }
}
