package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.coreutils.TaskHasNotCorrectState
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.domain.models.User.Companion.toText
import space.active.taskmanager1c.presentation.screens.task_detailed.TaskDetailedTaskState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Task(
    val date: String,
    val description: String,
    val endDate: String,
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
    val outputId: Int = 0
) {

    enum class Status {
        New, // не используется Можно использовать при отправке на сервер
        Accepted, // в работе - по умолчанию создаётся автором. может меняться исполнителем todo отображение snackbar с отменой
        Performed, // на доработке - ставит автор
        Reviewed, // условно завершена - ставит исполнитель todo отображение snackbar с отменой
        Finished, // принята - ставит автор
        Deferred, // отложена используется редко - испольнитель todo иконка для испольнителя bottom menu
        Cancelled // отклоненная - не используется
    }

    fun toTaskInput(new: Boolean = false) = TaskInput(
        date = this.date,
        description = this.description,
        endDate = this.endDate,
        id = if (new) {
            this.hashCode().toString()
        } else {
            this.id
        },
        mainTaskId = this.mainTaskId,
        name = this.name,
        number = this.number,
        objName = this.objName,
        photos = this.photos,
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
        startDate = this.date,
        number = this.number,
        author = this.users.author.name,
        deadLine = this.endDate,
        daysEnd = this.getDeadline(),
        performer = this.users.performer.name,
        coPerfomers = this.users.coPerformers.toText(),
        observers = this.users.observers.toText(),
        description = this.description,
        taskObject = this.objName,
        mainTask = this.mainTaskId, // todo add inner task
        status = this.status
    )

    fun fromTaskStatus(status: Status): String {
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
    fun getDeadline(): String {
        val end = this.endDate
        if (end.isNotBlank()) {
            val today = LocalDate.now().toEpochDay()
            try {
                // 2022-04-07T00:52:37
                val endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val end = endDate.toEpochDay()
                val difference: Long = end - today
                return "${difference.toString()} дней"
            } catch (e: Exception) {
                return e.message.toString()
            }
        } else {
            return ""
        }
    }

    companion object {

        fun newTask(author: User) = Task(
            date = LocalDate.now().toString(),
            endDate = LocalDate.now().toString(),
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
                "new" -> {
                    Status.New
                }
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



