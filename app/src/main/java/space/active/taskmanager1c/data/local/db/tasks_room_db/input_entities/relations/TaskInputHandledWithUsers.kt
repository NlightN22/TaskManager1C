package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import space.active.taskmanager1c.coreutils.toZonedDateTime
import space.active.taskmanager1c.coreutils.toZonedDateTimeOrNull
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.*
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
    fun isNewVersion(old: TaskInputHandledWithUsers): Boolean = this.taskInput.version > old.taskInput.version

    fun toTaskDomain(listUsersInput: List<UserInput>): TaskDomain {
        with(this.taskInput) {
            return TaskDomain(
                date = date.toZonedDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                description = description,
                endDate = endDate?.toZonedDateTimeOrNull(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                id = id,
                mainTaskId = mainTaskId ?: "",
                name = name,
                number = number,
                objName = objName ?: "",
                priority = priority.toDomainPriority(),
                status = TaskDomain.toTaskStatus(
                    status
                ),
                unreadTag = unreadTag,
                users = toUsersDomain(listUsersInput),
                isAuthor = isAuthor,
                isPerformer = isPerformer,
                ok = ok,
                cancel = cancel,
                version = version
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
                unreaded = unreadTag,
                version = version
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
        return this.find { it.userId == id }?.toUserDomain() ?: UserDomain(id = id, name = id)
    }

    private fun String.toDomainPriority(): TaskDomain.Priority {
        return when (this) {
            "high" -> {TaskDomain.Priority.High}
            "low" -> {TaskDomain.Priority.Low}
            else -> {TaskDomain.Priority.Middle}
        }
    }
}


