package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInputHandled
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskInputHandledWithUsers
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.remote.model.TaskDto
import space.active.taskmanager1c.domain.models.UserDomain.Companion.toText
import space.active.taskmanager1c.presentation.screens.task_detailed.TaskState
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class TaskDomain(
    val date: ZonedDateTime,
    val description: String,
    val endDate: ZonedDateTime?,
    val id: String,
    val mainTaskId: String,
    val name: String,
    val number: String,
    val objName: String,
    val priority: Priority,
    val status: Status,
    val users: UsersInTaskDomain,
    val isSending: Boolean = false,
    val outputId: Int = 0,
    val unread: Boolean = false,
    val isAuthor: Boolean,
    val isPerformer: Boolean,
    val ok: Boolean,
    val cancel: Boolean
) {

    enum class Priority {
        High,
        Middle,
        Low;
        fun toDTO(): String {
            return when (this) {
                High -> "high"
                Middle -> "middle"
                Low -> "low"
            }
        }
    }

    enum class Status {
        New, // не используется Можно использовать при отправке на сервер
        Accepted, // в работе - по умолчанию создаётся автором. может меняться исполнителем
        Performed, // на доработке - ставит автор только для Reviewed
        Reviewed, // условно завершена - ставит исполнитель
        Finished, // принята - ставит автор только для Reviewed
        Deferred, // отложена используется редко - испольнитель todo иконка для испольнителя bottom menu in Accepted status
        Cancelled; // отклоненная - не используется

        fun getResId(): Int {
            return when (this) {
                New -> R.string.New
                Accepted -> R.string.Accepted
                Performed -> R.string.Performed
                Reviewed -> R.string.Reviewed
                Finished -> R.string.Finished
                Deferred -> R.string.Deferred
                Cancelled -> R.string.Cancelled
            }
        }

        fun toStatusDTO(): String {
            return when (this) {
                New -> "new"
                Accepted -> "accepted"
                Performed -> "performed"
                Reviewed -> "reviewed"
                Finished -> "finished"
                Deferred -> "deferred"
                Cancelled -> "cancelled"
            }
        }
    }

    private fun toTaskInputHandledWithUsers(new: Boolean = false) = TaskInputHandledWithUsers(
        taskInput = this.toTaskInputHandled(new),
        coPerformers = users.coPerformers.map { it.toCoPerformer(id) },
        observers = users.observers.map { it.toObservers(id) }
    )

    private fun toTaskInputHandled(new: Boolean = false) = TaskInputHandled(
        date = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        description = description,
        endDate = endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) ?: "",
        id = if (new) {
            hashCode().toString()
        } else {
            id
        },
        mainTaskId = mainTaskId,
        name = name,
        number = number,
        objName = objName,
        priority = priority.toDTO(),
        status = status.toStatusDTO(),
        authorId = users.author.id,
        performerId = users.performer.id,
        isAuthor = isAuthor,
        isPerformer = isPerformer,
        ok = ok,
        cancel = cancel,
    )

    fun toOutputTask(new: Boolean = false): OutputTask {
        return OutputTask(
            newTask = new,
            taskDto = toTaskDTO()
        )
    }

    fun toTaskDTO(): TaskDto {
        return TaskDto(
            authorId = users.author.id,
            coPerformers = users.coPerformers.map { it.id },
            date = date.toDTO(),
            description = description,
            endDate = endDate.toDTO(),
            id = id,
            mainTaskId = mainTaskId,
            name = name,
            number = number,
            objName = objName,
            observers = users.observers.map { it.id },
            performerId = users.performer.id,
            priority = priority.toDTO(),
            status = status.toStatusDTO(),
        )
    }

    fun toTaskState() = TaskState(
        id = this.id,
        title = this.name,
        startDate = this.date.toShortDateTime(),
        number = this.number,
        author = this.users.author.name,
        deadLine = this.endDate?.toShortDate() ?: "",
        daysEnd = this.getDeadline(),
        performer = this.users.performer.name,
        coPerfomers = this.users.coPerformers.toText(),
        observers = this.users.observers.toText(),
        description = this.description,
        taskObject = this.objName,
        mainTask = this.mainTaskId, // todo add inner taskDomain
        status = this.status
    )


    /**
     * Return days deadline in string
     */
    private fun getDeadline(): String {
        this.endDate?.let {
            val difference = it.nowDiffInDays()
            return "$difference дней"
        } ?: kotlin.run {
            return ""
        }
    }

    companion object {
        fun newTask(author: UserDomain) = TaskDomain(
            date = ZonedDateTime.now(),
            endDate = ZonedDateTime.now(),
            users = UsersInTaskDomain(
                author = author,
                performer = UserDomain.blankUser(),
                coPerformers = emptyList(),
                observers = emptyList()
            ),
            description = "",
            id = "",
            mainTaskId = "",
            name = "",
            number = "",
            objName = "",
            priority = Priority.Middle,
            status = Status.New,
            isAuthor = true,
            isPerformer = false,
            unread = false,
            ok = true,
            cancel = false
        )

        fun toTaskStatus(status: String): Status {
            return when (status) {
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
    }
}



