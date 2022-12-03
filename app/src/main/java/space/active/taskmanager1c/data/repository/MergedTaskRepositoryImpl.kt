package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.Task.Companion.mapAndReplaceById
import space.active.taskmanager1c.domain.repository.TasksRepository

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
            outputTaskRepository.outputTaskList
        ) { inputList, outputList ->
            combineListTasks(inputList, outputList)
        }.flowOn(ioDispatcher)


    override fun getTask(taskId: String): Flow<Request<Task>> =
        combine(
            inputTaskRepository.getTaskFlow(taskId), outputTaskRepository.getTaskFlow(taskId)

        ) { inputTask, outputTask ->
            val combineResult = taskCombine(inputTask, outputTask)
            if (combineResult != null) {
                SuccessRequest(combineResult)
            } else {
                /**
                 * return if empty base
                 */
                ErrorRequest<Task>(EmptyObject)
            }
        }.flowOn(ioDispatcher)


    private suspend fun taskCombine(inputTask: TaskInput?, outputTask: OutputTask?): Task? {

        outputTask?.let { outputTask ->
            val convertedOutput = Task.fromTaskOutput(outputTask)
            if (inputTask != null) {
                val convertedInput = Task.fromTaskInput(inputTask)
                /**
                 * return if tasks are same. It's unbelievable, but still...
                 */
                if (convertedInput == convertedOutput) {
                    outputTaskRepository.deleteTask(outputTask)
                    return convertedInput
                } else {
                    /**
                     * return if tasks are different.
                     */
                    return convertedOutput
                }
            } else {
                /**
                 * return if input task is null
                 */
                return convertedOutput
            }
        } ?: return if (inputTask != null) {
            /**
             * return if output task is null
             */
            Task.fromTaskInput(inputTask)
        } else {
            null
        }
    }

    private fun getTaskCombineOld(taskId: String) = flow<Request<Task>> {
        // TODO replace to flow combine
        emit(PendingRequest())
        /**
         * If the task is new we can't edit it until it's submitted to server
         */
        if (taskId.isNotEmpty() or taskId.isNotBlank()) {

            var resultOutputList = listOf<OutputTask>()
            var resultInputList = listOf<TaskInput>()

            // у новых исходящих задач, не будет id входящей
            val resultOutput = outputTaskRepository.getTask(taskId)
            if (resultOutput != null) {
                resultOutputList = listOf<OutputTask>(resultOutput)
            }
            val resultInput = inputTaskRepository.getTask(taskId)
            if (resultInput != null) {
                resultInputList = listOf<TaskInput>(resultInput)
            }

            val finalTask = combineListTasks(resultInputList, resultOutputList).firstOrNull()
            if (finalTask != null) {
                emit(SuccessRequest(finalTask))
            } else {
                emit(ErrorRequest(EmptyObject))
            }
        } else {
            emit(ErrorRequest(ThisTaskIsNotEdited))
        }
    }.catch { e ->
        ErrorRequest<Task>(e)
    }.flowOn(ioDispatcher)

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
                emit(ErrorRequest(ThisTaskIsNotEdited))
            }
        }
    }.catch { e ->
        emit(ErrorRequest(e))
    }.flowOn(ioDispatcher)

    override fun createNewTask(task: Task) = flow<Request<Any>> {
        // При отправке у меня нет taskInput id.
        // А значит при получении задач с сервера и удалении задачи из исходящего кэша
        // Может быть такая ситуация при которой пользователь будет находиться
        // в новой сохраненной для отправки задачи. И при получении её в качестве подтверждения она уже будет считаться другой...
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
    }.catch { e ->
        emit(ErrorRequest(e))
    }.flowOn(ioDispatcher)

    override fun attachFileToTask(file: ByteArray, taskId: String) = flow<Request<Any>> {
        TODO("Not yet implemented")
    }.flowOn(ioDispatcher)

    private fun combineListTasks(
        taskIn: List<TaskInput>,
        taskOut: List<OutputTask>
    ): List<Task> {
        if (taskIn.isNotEmpty() or taskOut.isNotEmpty()) {
            /**
             * Take OutputTask and find the same in InputTasks.
             * Replace changed and not submitted in InputTasks from OutputTask
             * Add new tasks from OutputTask to InputTasks
             */
            if (taskOut.isNotEmpty()) {
                /**
                 * Data type casting to Task
                 */

                val convertedTaskInput = Task.fromTaskInputList(taskIn)
                val convertedTaskOutput = Task.fromTaskOutputList(taskOut)

                /**
                 *  Find in task input list the same tasks by id and replace them by output tasks
                 */
                val replacedTaskInputList =
                    convertedTaskInput.mapAndReplaceById(convertedTaskOutput)

                /**
                 * Add new output tasks to final list
                 */
                return replacedTaskInputList.addNotContained(convertedTaskOutput)
            } else if (taskIn.isNotEmpty()) {
                return Task.fromTaskInputList(taskIn)
            }
        }
        return emptyList()
    }
}