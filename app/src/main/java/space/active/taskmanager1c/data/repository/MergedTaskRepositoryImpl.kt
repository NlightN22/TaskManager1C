package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.OutputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.SortField
import space.active.taskmanager1c.data.local.db.tasks_room_db.SortType
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInputHandled
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput.Companion.toListUserDomain
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.domain.models.TaskDomain
import space.active.taskmanager1c.domain.models.TaskListFilterTypes
import space.active.taskmanager1c.domain.models.TaskListOrderTypes
import space.active.taskmanager1c.domain.models.UserDomain
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Singleton

private const val TAG = "MergedTaskRepositoryImpl"

@Singleton
class MergedTaskRepositoryImpl constructor(
    private val inputTaskRepository: InputTaskRepository,
    private val outputTaskRepository: OutputTaskRepository,
    private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger
) : TasksRepository {

    // todo delete
    override val listTasksFlow: Flow<List<TaskDomain>> =
        combine(
            inputTaskRepository.listTaskFlow,
            outputTaskRepository.outputTaskList,
        ) { inputList, outputList ->
            combineListTasks(inputList, outputList)
        }
            .flowOn(ioDispatcher)

    override fun getTasksFiltered(
        filterTypes: Flow<TaskListFilterTypes>,
        orderTypes: Flow<TaskListOrderTypes>,
        myIdFlow: Flow<String>
    ): Flow<List<TaskDomain>> = orderTypes.flatMapLatest { order ->
        filterTypes.flatMapLatest { filter ->
            val myId = myIdFlow.first()
            val sortField = order.getSortFieldAndType().first
            val sortType = order.getSortFieldAndType().second
            selectFilter(filter, myId, sortField, sortType)
        }
    }.combine(outputTaskRepository.outputTaskList) {
            inputList, outputList ->
        combineListTasks(inputList, outputList)
    }.flowOn(ioDispatcher)

    private fun selectFilter(
        filter: TaskListFilterTypes,
        myId: String,
        sortField: SortField,
        sortType: SortType
    ): Flow<List<TaskInputHandled>> {
        return when (filter) {
            is TaskListFilterTypes.IDo -> inputTaskRepository.filteredIdo(myId, sortField, sortType)
            is TaskListFilterTypes.IDelegate -> inputTaskRepository.filteredIDelegate(
                myId,
                sortField,
                sortType
            )
            is TaskListFilterTypes.IDidNtCheck -> inputTaskRepository.filteredIDidNtCheck(
                myId,
                sortField,
                sortType
            )
            is TaskListFilterTypes.IObserve -> inputTaskRepository.filteredIObserve(
                myId,
                sortField,
                sortType
            )
            is TaskListFilterTypes.IDidNtRead -> inputTaskRepository.filteredIDidNtRead(
                sortField,
                sortType
            )
            is TaskListFilterTypes.All -> inputTaskRepository.sortedAll(sortField, sortType)
        }
    }

    override val listUsersFlow: Flow<List<UserDomain>> =
        inputTaskRepository.listUsersFlow.map { it.toListUserDomain() }

    override fun getTask(taskId: String): Flow<TaskDomain?> = getCombinedTask(taskId)

    private fun getCombinedTask(taskId: String) = combine(
        inputTaskRepository.getTaskFlow(taskId),
        outputTaskRepository.getTaskFlow(taskId),
    ) { inputTask, outputTask ->
        taskCombine(inputTask, outputTask)
    }.flowOn(ioDispatcher)

    // todo delete
//    private fun getInputTask(taskId: String) = inputTaskRepository.getTaskFlow(taskId).map {
//        logger.log(TAG, "return ${it?.toTaskDomain()?.name}")
//        it?.toTaskDomain()
//    }

    private suspend fun taskCombine(
        inputTask: TaskInputHandled?,
        outputTask: OutputTask?
    ): TaskDomain? {
        val usersInputList = inputTaskRepository.getUsers()
        outputTask?.let { output ->
            val convertedOutput: TaskInput = output.taskInput
            inputTask?.let {
                val convertedInput: TaskInput = inputTask.taskIn
                /**
                 * return if taskDomains are same. It's unbelievable, but still...
                 */
                if (convertedInput == convertedOutput) {
                    outputTaskRepository.deleteTask(output)
                    logger.log(TAG, "return convertedInput")
                    return it.copy(taskIn = convertedInput).toTaskDomain(usersInputList)
                } else {
                    /**
                     * return if taskDomains are different.
                     */
                    logger.log(TAG, "return convertedOutput")
                    return it.copy(taskIn = convertedOutput).toTaskDomain(usersInputList)
                }
            } ?: kotlin.run {
                /**
                 * return if input taskDomain is null
                 */
                return null
            }
        } ?: inputTask?.let {
            /**
             * return if output taskDomain is null
             */
            logger.log(TAG, "return inExtraTask")
            logger.log(TAG, "return ${it.toTaskDomain(usersInputList).name}")
            return it.toTaskDomain(usersInputList)
        } ?: kotlin.run {
            return null
        }
    }

    override fun editTask(taskDomain: TaskDomain) = flow<Request<Any>> {
        emit(PendingRequest())
        /**
         * Check for not new taskDomain
         */
        if (taskDomain.isSending) {
            emit(ErrorRequest(TaskIsNewAndInSendingState))
        } else {
            if (taskDomain.id.isNotEmpty() or taskDomain.id.isNotBlank()) {
                outputTaskRepository.insertTask(taskDomain.toOutputTask())
                emit(SuccessRequest(Any()))
            } else {
                emit(ErrorRequest(ThisTaskIsNotEdited(taskDomain.name)))
            }
        }
    }.catch { e ->
        emit(ErrorRequest(e))
    }.flowOn(ioDispatcher)

    override fun createNewTask(taskDomain: TaskDomain) = flow<Request<Any>> {
        emit(PendingRequest())
        /**
         * Check for new taskDomain
         */
        if (taskDomain.id.isEmpty() or taskDomain.id.isBlank()) {
            outputTaskRepository.insertTask(taskDomain.toOutputTask(new = true))
            emit(SuccessRequest(Any()))
        } else {
            emit(ErrorRequest(ThisTaskIsNotNew))
        }
    }.flowOn(ioDispatcher)

    override fun attachFileToTask(file: ByteArray, taskId: String) = flow<Request<Any>> {
        TODO("Not yet implemented")
    }.flowOn(ioDispatcher)

    private suspend fun combineListTasks(
        taskInput: List<TaskInputHandled>,
        taskOut: List<OutputTask>
    ): List<TaskDomain> {
        val usersInputList = inputTaskRepository.getUsers()
        if (taskInput.isNotEmpty() or taskOut.isNotEmpty()) {
            /**
             * Take OutputTask and find the same in InputTasks.
             * Replace changed and not submitted in InputTasks from OutputTask
             * Add new taskDomains from OutputTask to InputTasks
             */
            if (taskOut.isNotEmpty()) {
                /**
                 * Data type casting to TaskDomain
                 */
                val convertedTaskOutputDomain: List<TaskInput> = taskOut.map { it.taskInput }
                /**
                 *  Find in taskDomain input list the same taskDomains by id and replace them by output taskDomains
                 *  Add new output taskDomains to final list
                 */
                return taskInput.map { ex ->
                    ex.copy(taskIn = taskInput.map { it.taskIn }
                        .mapAndReplaceById(convertedTaskOutputDomain)
                        .addNotContainedFromList(convertedTaskOutputDomain)
                        .find { input -> input.id == ex.taskIn.id }
                        ?: ex.taskIn)
                }
                    .map { it.toTaskDomain(usersInputList) }
            } else if (taskInput.isNotEmpty()) {
                return taskInput.map { it.toTaskDomain(usersInputList) }
            }
        }
        return emptyList()
    }

    // todo delete
    private suspend fun getUserTaskInput(userId: String): UserDomain =
        inputTaskRepository.getUser(userId)?.toUserDomain()
            ?: UserDomain(id = userId, name = userId)


    private fun List<TaskInput>.mapAndReplaceById(newList: List<TaskInput>): List<TaskInput> {
        return this.map { list1Item ->
            newList.find { list2Item -> (list1Item.id == list2Item.id) } ?: list1Item
        }

    }
}