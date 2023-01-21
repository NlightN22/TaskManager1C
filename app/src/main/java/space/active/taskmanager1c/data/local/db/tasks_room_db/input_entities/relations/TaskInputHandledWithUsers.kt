package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import space.active.taskmanager1c.coreutils.toDateTime
import space.active.taskmanager1c.coreutils.toDateTimeOrNull
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.CoPerformersInTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.ObserversInTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInputHandled
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.remote.model.TaskDto
import space.active.taskmanager1c.domain.models.TaskDomain
import space.active.taskmanager1c.domain.models.UserDomain
import space.active.taskmanager1c.domain.models.UsersInTaskDomain
import java.time.format.DateTimeFormatter

data class TaskInputHandledWithUsers(
    @Embedded val taskInput: TaskInputHandled,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val coPerformers: List<CoPerformersInTask>,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val observers: List<ObserversInTask>
) {
    fun toTaskDomain(listUsersInput: List<UserInput>): TaskDomain {
        with(this.taskInput) {
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
                users = toUsersDomain(listUsersInput),
                isAuthor = isAuthor,
                isPerformer = isPerformer,
                ok = ok,
                cancel = cancel
            )
        }
    }

    fun toTaskDTO(): TaskDto  {
        with(this.taskInput) {
            return TaskDto(
                authorId =authorId,
                coPerformers =coPerformers.map { it.coPerformerId },
                date = date,
                description = description,
                endDate = endDate ?: "",
                id = id,
                mainTaskId = mainTaskId ?: "",
                name = name,
                number = number,
                objName = objName ?: "",
                observers = observers.map { it.observerId },
                performerId = performerId,
                priority = priority ?: "",
                status = status,
            )
        }
    }

    fun toUsersDomain(listUsers: List<UserInput>): UsersInTaskDomain = UsersInTaskDomain(
        author = listUsers.toUserDomain(this.taskInput.authorId),
        performer = listUsers.toUserDomain(this.taskInput.performerId),
        coPerformers = coPerformers.map { listUsers.toUserDomain(it.coPerformerId) },
        observers = observers.map { listUsers.toUserDomain(it.observerId) }

    )

    private fun List<UserInput>.toUserDomain(id: String): UserDomain {
        return this.find { it.id == id }?.toUserDomain() ?: UserDomain(id = id, name = id)
    }
}
