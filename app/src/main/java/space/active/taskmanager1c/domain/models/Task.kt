package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.coreutils.TaskHasNotCorrectState
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput.Companion.mapAndReplaceById
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

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
        New,
        Accepted,
        Performed,
        Reviewed,
        Finished,
        Deferred,
        Cancelled
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
            OutputTask(taskInput = this.toTaskInput(),
            outputId = this.outputId
                )
        }
    }

    fun fromTaskStatus(status: Status): String {
        return when (status) {
            Status.New -> "new"
            Status.Accepted -> "accepted"
            Status.Performed -> "performed"
            Status.Reviewed  -> "reviewed"
            Status.Finished -> "finished"
            Status.Deferred -> "deferred"
            Status.Cancelled -> "cancelled"
        }
    }

    companion object {
//        fun fromTaskInput(taskInput: TaskInput): Task = Task(
//            date = taskInput.date,
//            description = taskInput.description,
//            endDate = taskInput.endDate,
//            id = taskInput.id,
//            mainTaskId = taskInput.mainTaskId,
//            name = taskInput.name,
//            number = taskInput.number,
//            objName = taskInput.objName,
//            photos = taskInput.photos,
//            priority = taskInput.priority,
//            status = toTaskStatus(taskInput.status),
//            users = UsersInTaskDomain.fromInputTask(taskInput.usersInTask),
//        )

//        fun fromTaskInputList(taskInputList: List<TaskInput>): List<Task> =
//            taskInputList.map { fromTaskInput(it) }

//        fun fromTaskOutput(taskOutput: OutputTask): Task = Task(
//            date = taskOutput.taskInput.date,
//            description = taskOutput.taskInput.description,
//            endDate = taskOutput.taskInput.endDate,
//            id = taskOutput.taskInput.id,
//            mainTaskId = taskOutput.taskInput.mainTaskId,
//            name = taskOutput.taskInput.name,
//            number = taskOutput.taskInput.number,
//            objName = taskOutput.taskInput.objName,
//            photos = taskOutput.taskInput.photos,
//            priority = taskOutput.taskInput.priority,
//            status = toTaskStatus(taskOutput.taskInput.status),
//            users = UsersInTaskDomain.fromInputTask(taskOutput.taskInput.usersInTask),
//            isSending = taskOutput.newTask
//        )

//        fun fromTaskOutputList(taskOutputList: List<OutputTask>): List<Task> =
//            taskOutputList.map { fromTaskOutput(it) }

        fun List<Task>.mapAndReplaceById(inputList: List<Task>): List<Task> {
            val replacedList = this.map { list1Item ->
                inputList.find { list2Item -> (list1Item.id == list2Item.id) } ?: list1Item
            }
            return replacedList
        }

        fun toTaskStatus(status: String): Status {
            return when (status) {
                "new" -> {Status.New}
                "accepted" -> Status.Accepted
                "performed" -> Status.Performed
                "reviewed" -> Status.Reviewed
                "finished" -> Status.Finished
                "deferred" -> Status.Deferred
                "cancelled" -> Status.Cancelled
                else ->  throw TaskHasNotCorrectState
            }
        }

    }
}



