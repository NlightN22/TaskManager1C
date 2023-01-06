package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.OutputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput.Companion.toListUserDomain
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.TaskInAndExtra
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.domain.models.UsersInTaskDomain
import space.active.taskmanager1c.domain.repository.TasksRepository
import java.time.format.DateTimeFormatter

private const val TAG = "MergedTaskRepositoryImpl"

class MergedTaskRepositoryImpl(
    private val inputTaskRepository: InputTaskRepository,
    private val outputTaskRepository: OutputTaskRepository,
    private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger
) : TasksRepository {

    override val listTasksFlow: Flow<List<Task>> =
        combine(
            inputTaskRepository.listTaskFlow,
            outputTaskRepository.outputTaskList,
        ) { inputList, outputList ->
            combineListTasks(inputList, outputList)
        }
            .flowOn(ioDispatcher)

    override val listUsersFlow: Flow<List<User>> =
        inputTaskRepository.listUsersFlow.map { it.toListUserDomain() }

    override fun getTask(taskId: String): Flow<Task?> =
        combine(
            inputTaskRepository.getTaskFlow(taskId),
            outputTaskRepository.getTaskFlow(taskId),
        ) { inputTask, outputTask ->
            taskCombine(inputTask, outputTask)
        }.flowOn(ioDispatcher)


    private suspend fun taskCombine(inExtraTask: TaskInAndExtra?, outputTask: OutputTask?): Task? {

        outputTask?.let { output ->
            val convertedOutput: TaskInput = output.taskInput
            inExtraTask?.let {
                val convertedInput: TaskInput = inExtraTask.taskIn
                /**
                 * return if tasks are same. It's unbelievable, but still...
                 */
                if (convertedInput == convertedOutput) {
                    outputTaskRepository.deleteTask(output)
                    return it.copy(taskIn = convertedInput).toTaskDomain()
                } else {
                    /**
                     * return if tasks are different.
                     */
                    return it.copy(taskIn = convertedOutput).toTaskDomain()
                }
            } ?: kotlin.run {
                /**
                 * return if input task is null
                 */
                return null
            }
        } ?: inExtraTask?.let {
            /**
             * return if output task is null
             */
            return it.toTaskDomain()
        } ?: kotlin.run {
            return null
        }
    }

    override fun editTask(task: Task) = flow<Request<Any>> {
        // TODO replace to flow combine
        emit(PendingRequest())
        /**
         * Check for not new task
         */
        if (task.isSending) {
            emit(ErrorRequest(TaskIsNewAndInSendingState))
        } else {
            if (task.id.isNotEmpty() or task.id.isNotBlank()) {
                outputTaskRepository.insertTask(task.toOutputTask())
                emit(SuccessRequest(Any()))
            } else {
                emit(ErrorRequest(ThisTaskIsNotEdited(task.name)))
            }
        }
    }.catch { e ->
        emit(ErrorRequest(e))
    }.flowOn(ioDispatcher)

    override fun createNewTask(task: Task) = flow<Request<Any>> {
        // При отправке у меня нет taskInput id.
        // А значит при получении задач с сервера и удалении задачи из исходящего кэша
        // Может быть такая ситуация при которой пользователь будет находиться
        // в новой сохраненной для отправки задаче.
        // И при получении её в качестве подтверждения она уже будет считаться другой...
        // какие есть варианты:
        // - Не давать менять новые задачи до момента отправки их на сервер и получения её с сервера
        // - Обрабатывать 3 статуса: Новая задача. Редактирование входящей задачи. Редактирование новой задачи.
        // - Менять задачи и их параметры "на лету", при получении новых данных с сервера.
        emit(PendingRequest())
        /**
         * Check for new task
         */
        if (task.id.isEmpty() or task.id.isBlank()) {
            outputTaskRepository.insertTask(task.toOutputTask(new = true))
            emit(SuccessRequest(Any()))
        } else {
            emit(ErrorRequest(ThisTaskIsNotNew))
        }
    }.flowOn(ioDispatcher)

    override fun attachFileToTask(file: ByteArray, taskId: String) = flow<Request<Any>> {
        TODO("Not yet implemented")
    }.flowOn(ioDispatcher)

    private suspend fun combineListTasks(
        taskInEx: List<TaskInAndExtra>,
        taskOut: List<OutputTask>
    ): List<Task> {
        if (taskInEx.isNotEmpty() or taskOut.isNotEmpty()) {
            /**
             * Take OutputTask and find the same in InputTasks.
             * Replace changed and not submitted in InputTasks from OutputTask
             * Add new tasks from OutputTask to InputTasks
             */
            if (taskOut.isNotEmpty()) {
                /**
                 * Data type casting to Task
                 */

//                val convertedTaskInput: List<TaskInput> = taskInEx.map { it.taskIn }
                val convertedTaskOutput: List<TaskInput> = taskOut.map { it.taskInput }

                /**
                 *  Find in task input list the same tasks by id and replace them by output tasks
                 */
                // todo delete
//                val replacedTaskInputList =
//                    convertedTaskInput.mapAndReplaceById(convertedTaskOutput)
//
//                val inputTaskList =
//                    replacedTaskInputList.addNotContainedFromList(convertedTaskOutput)

//                val tasInExList = taskInEx.map { ex ->
//                    ex.copy(taskIn = taskInEx.map { it.taskIn }
//                        .mapAndReplaceById(convertedTaskOutput)
//                        .addNotContainedFromList(convertedTaskOutput)
//                        .find { input -> input.id == ex.taskIn.id }
//                        ?: ex.taskIn)
//                }

//                val finalList = tasInExList.map { it.toTaskDomain() }
                /**
                 * Add new output tasks to final list
                 */
                return taskInEx.map { ex ->
                    ex.copy(taskIn = taskInEx.map { it.taskIn }
                        .mapAndReplaceById(convertedTaskOutput)
                        .addNotContainedFromList(convertedTaskOutput)
                        .find { input -> input.id == ex.taskIn.id }
                        ?: ex.taskIn)
                    }
                    .map { it.toTaskDomain() }
            } else if (taskInEx.isNotEmpty()) {
                return taskInEx.map { it.toTaskDomain() }
            }
        }
        return emptyList()
    }

    private suspend fun TaskInAndExtra.toTaskDomain(): Task {
        with(this.taskIn) {
            return Task(
                date = date.toDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                description = description,
                endDate = endDate?.toDateTimeOrNull(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                id = id,
                mainTaskId = mainTaskId ?: "",
                name = name,
                number = number,
                objName = objName ?: "",
                priority = priority,
                status = Task.toTaskStatus(status),
                users = UsersInTaskDomain
                    (
                    author = getUserTaskInput(usersInTask.authorId),
                    performer = getUserTaskInput(usersInTask.performerId),
                    coPerformers = usersInTask.coPerformers.map { getUserTaskInput(it) },
                    observers = usersInTask.observers.map { getUserTaskInput(it) },
                ),
                whoIsInTask = extra.whoIsInTask,
                unread = extra.unread,
                ok = extra.ok,
                cancel = extra.cancel
            )
        }
    }

    //todo delete
//    private suspend fun inputTaskToTaskDomain(inputTask: TaskInput) = Task(
//        date = inputTask.date.toDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
//        description = inputTask.description,
//        endDate = inputTask.endDate.toDateTimeOrNull(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
//        id = inputTask.id,
//        mainTaskId = inputTask.mainTaskId,
//        name = inputTask.name,
//        number = inputTask.number,
//        objName = inputTask.objName,
//        priority = inputTask.priority,
//        status = Task.toTaskStatus(inputTask.status),
//        users = UsersInTaskDomain
//            (
//            author = getUserTaskInput(inputTask.usersInTask.authorId),
//            performer = getUserTaskInput(inputTask.usersInTask.performerId),
//            coPerformers = inputTask.usersInTask.coPerformers.map { getUserTaskInput(it) },
//            observers = inputTask.usersInTask.observers.map { getUserTaskInput(it) },
//        )
//    )
    //todo delete
//    private suspend fun OutputTask.toTaskDomain(): Task {
//        with(this.taskInput)
//        {
//            Task(
//                date = date.toDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
//                description = taskInput.description,
//                endDate = endDate.toDateTimeOrNull(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
//                id = id,
//                mainTaskId = mainTaskId,
//                name = name,
//                number = number,
//                objName = objName,
//                priority = priority,
//                status = Task.toTaskStatus(status),
//                users = UsersInTaskDomain(
//                    author = getUserTaskInput(usersInTask.authorId),
//                    performer = getUserTaskInput(usersInTask.performerId),
//                    coPerformers = usersInTask.coPerformers.map {
//                        getUserTaskInput(
//                            it
//                        )
//                    },
//                    observers = usersInTask.observers.map { getUserTaskInput(it) },
//                ),
//                isSending = this@toTaskDomain.newTask,
//
//                )
//        }
//    }

    private suspend fun getUserTaskInput(userId: String): User =
        inputTaskRepository.getUser(userId)?.toUserDomain()
            ?: User(id = userId, name = userId)


    private fun List<TaskInput>.mapAndReplaceById(newList: List<TaskInput>): List<TaskInput> {
        return this.map { list1Item ->
            newList.find { list2Item -> (list1Item.id == list2Item.id) } ?: list1Item
        }

    }
}