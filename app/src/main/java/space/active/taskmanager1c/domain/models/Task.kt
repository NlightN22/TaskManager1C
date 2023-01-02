package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.TaskHasNotCorrectState
import space.active.taskmanager1c.coreutils.nowDiffInDays
import space.active.taskmanager1c.coreutils.toShortDate
import space.active.taskmanager1c.coreutils.toShortDateTime
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.domain.models.User.Companion.toText
import space.active.taskmanager1c.presentation.screens.task_detailed.TaskDetailedTaskState
import space.active.taskmanager1c.presentation.screens.tasklist.TasKForAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Task(
    val date: LocalDateTime,
    val description: String,
    val endDate: LocalDateTime?,
    val id: String,
    val mainTaskId: String,
    val name: String,
    val number: String,
    val objName: String,
    val photos: List<String> = emptyList(), // TODO replace
    val priority: String,
    val status: Status,
    val users: UsersInTaskDomain,
    val isSending: Boolean = false,
    val outputId: Int = 0,
    val unread: Boolean = false
) {

    fun toTaskAdapter(status: TasKForAdapter.Status, whoAmI: User) = TasKForAdapter(
        task = this,
        status = status,
        showUser = if (whoAmI == this.users.author) {
            this.users.performer
        } else {
            this.users.author
        }
    )



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
    }


    private fun toTaskInput(new: Boolean = false) = TaskInput(
        date = this.date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        description = this.description,
        endDate = this.endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) ?: "",
        id = if (new) {
            this.hashCode().toString()
        } else {
            this.id
        },
        mainTaskId = this.mainTaskId,
        name = this.name,
        number = this.number,
        objName = this.objName,
        priority = this.priority,
        status = this.fromTaskStatus(this.status),
        usersInTask = this.users.toTaskInput(),
    )

    fun toOutputTask(new: Boolean = false): OutputTask {
        return if (new) {
            OutputTask(newTask = new, taskInput = this.toTaskInput(new))
        } else {
            OutputTask(
                taskInput = this.toTaskInput(),
                outputId = this.outputId
            )
        }
    }

    fun toTaskState() = TaskDetailedTaskState(
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
        mainTask = this.mainTaskId, // todo add inner task
        status = this.status
    )

    private fun fromTaskStatus(status: Status): String {
        return when (status) {
            Status.New -> "new"
            Status.Accepted -> "accepted"
            Status.Performed -> "performed"
            Status.Reviewed -> "reviewed"
            Status.Finished -> "finished"
            Status.Deferred -> "deferred"
            Status.Cancelled -> "cancelled"
        }
    }

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

        fun newTask(author: User) = Task(
            date = LocalDateTime.now(),
            endDate = LocalDateTime.now(),
            users = UsersInTaskDomain(
                author = author,
                performer = User.blankUser(),
                coPerformers = emptyList(),
                observers = emptyList()
            ),
            description = "",
            id = "",
            mainTaskId = "",
            name = "",
            number = "",
            objName = "",
            priority = "",
            status = Task.Status.New,
        )

        fun List<Task>.mapAndReplaceById(inputList: List<Task>): List<Task> {
            val replacedList = this.map { list1Item ->
                inputList.find { list2Item -> (list1Item.id == list2Item.id) } ?: list1Item
            }
            return replacedList
        }

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



