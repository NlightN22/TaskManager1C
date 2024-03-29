package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.OutputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.FilterType
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput.Companion.toListUserDomain
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskInputHandledWithUsers
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.remote.model.TaskDto
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

    override suspend fun getInputTasksCount(): Int = inputTaskRepository.getInputTasksCount()

    override fun getTasksFiltered(
        filterTypes: Flow<TaskListFilterTypes>,
        orderTypes: Flow<TaskListOrderTypes>,
        myId: Flow<String>
    ): Flow<List<TaskDomain>> = getTasksFromBothRepo(filterTypes,orderTypes,myId)

    override fun getInnerTasks(taskId: String): Flow<List<TaskDomain>> =
        inputTaskRepository.getInnerTasks(taskId).map { it.map { it.toTaskDomain(inputTaskRepository.getUsers()) } }
            .flowOn(ioDispatcher)

    private fun getTasksFromBothRepo(
        filterTypes: Flow<TaskListFilterTypes>,
        orderTypes: Flow<TaskListOrderTypes>,
        myId: Flow<String>
    ): Flow<List<TaskDomain>> =
        orderTypes
            .distinctUntilChanged()
            .flatMapLatest { order ->
                filterTypes
                    .debounce(300)
                    .distinctUntilChanged()
                    .flatMapLatest { filter ->
                        val myIdCur = myId.first()
                        val sortField = order.getSortFieldAndType().first
                        val sortType = order.getSortFieldAndType().second
                        val filterType: FilterType = filter.toFilterType()
                        inputTaskRepository.sortedQuery(myIdCur,filterType,sortField, sortType)
                    }
            }.combine(outputTaskRepository.outputTaskList) { inputList, outputList ->
                val myIdCur = myId.first()
                combineListTasks(inputList, outputList, myIdCur)
            }.combine(inputTaskRepository.getUnreadIds()) {
                    input, unReadingIds ->
                // replace reading state
                input.map { if (unReadingIds.contains(it.id)) {it.copy(unread = true)} else {it} }
            }
            .flowOn(ioDispatcher)

    override val listUsersFlow: Flow<List<UserDomain>> =
        inputTaskRepository.listUsersFlow.map { it.toListUserDomain() }

    override fun getTask(taskId: String): Flow<TaskDomain?> = getCombinedTask(taskId)

    private fun getCombinedTask(taskId: String) = combine(
        inputTaskRepository.getTaskFlow(taskId),
        outputTaskRepository.getTaskFlow(taskId),
    ) { inputTask, outputTask ->
        taskCombine(inputTask, outputTask)
    }.flowOn(ioDispatcher)

    private suspend fun taskCombine(
        inputTask: TaskInputHandledWithUsers?,
        outputTask: OutputTask?
    ): TaskDomain? {
        val usersInputList = inputTaskRepository.getUsers()
        outputTask?.let { output ->
            val convertedOutput: TaskDto = output.taskDto
            inputTask?.let { input->
                val convertedInput: TaskDto = inputTask.toTaskDTO()
                /**
                 * return if taskDomains are same. It's unbelievable, but still...
                 */
                if (convertedInput == convertedOutput) {
                    outputTaskRepository.deleteTask(output)
                    logger.log(TAG, "return equal inputTask")
                    return input.toTaskDomain(usersInputList)
                } else {
                    /**
                     * return if taskDomains are different.
                     */
                    logger.log(TAG, "return priority outputTask")
                    return output.taskDto.toTaskInputHandledWithUsers(output.myId).toTaskDomain(usersInputList)
                }
            } ?: kotlin.run {
                /**
                 * return if input taskDomain is null
                 */
                return null
            }
        } ?: inputTask?.let { input ->
            /**
             * return if output taskDomain is null
             */
            logger.log(TAG, "return only inputTask")
            return input.toTaskDomain(usersInputList)
        } ?: kotlin.run {
            logger.log(TAG, "return null")
            return null
        }
    }

    override fun editTask(taskDomain: TaskDomain, myId: String) = flow<Request<Any>> {
        emit(PendingRequest())
        /**
         * Check for not new taskDomain
         */
        if (taskDomain.isSending) {
            emit(ErrorRequest(TaskIsNewAndInSendingState))
        } else {
            if (taskDomain.id.isNotEmpty() or taskDomain.id.isNotBlank()) {
                val insertedTask = taskDomain.toOutputTask(myId)
                logger.log(TAG, "insertTask to DB: $insertedTask")
                outputTaskRepository.insertTask(insertedTask)
                emit(SuccessRequest(Any()))
            } else {
                emit(ErrorRequest(ThisTaskIsNotEdited(taskDomain.name)))
            }
        }
    }.catch { e ->
        emit(ErrorRequest(e))
    }.flowOn(ioDispatcher)

    override fun createNewTask(taskDomain: TaskDomain, myId: String) = flow<Request<Any>> {
        emit(PendingRequest())
        /**
         * Check for new taskDomain
         */
        if (taskDomain.id.isEmpty() or taskDomain.id.isBlank()) {
            outputTaskRepository.insertTask(taskDomain.toOutputTask(myId, new = true))
            emit(SuccessRequest(Any()))
        } else {
            emit(ErrorRequest(ThisTaskIsNotNew))
        }
    }.flowOn(ioDispatcher)

    private suspend fun combineListTasks(
        taskInput: List<TaskInputHandledWithUsers>,
        taskOut: List<OutputTask>,
        myId: String
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
                val convertedTaskOutputDomain: List<TaskInputHandledWithUsers> =
                    taskOut.map { it.taskDto.toTaskInputHandledWithUsers(myId) }
                /**
                 *  Find in taskDomain input list the same taskDomains by id and replace them by output taskDomains
                 *  Add new output taskDomains to final list
                 */
                return taskInput.mapAndReplaceById(convertedTaskOutputDomain)
                    .addNotContainedFromList(convertedTaskOutputDomain)
                    .map { it.toTaskDomain(usersInputList) }
            } else if (taskInput.isNotEmpty()) {
                return taskInput.map { it.toTaskDomain(usersInputList) }
            }
        }
        return emptyList()
    }

    private fun List<TaskInputHandledWithUsers>.mapAndReplaceById(newList: List<TaskInputHandledWithUsers>): List<TaskInputHandledWithUsers> {
        return this.map { list1Item ->
            newList.find { list2Item -> (list1Item.taskInput.id == list2Item.taskInput.id) }
                ?: list1Item
        }

    }
}