package space.active.taskmanager1c.data.local.db.tasks_room_db

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInputHandled
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "InputTaskRepositoryImpl"

@Singleton
class InputTaskRepositoryImpl @Inject constructor(
    private val inputDao: TaskInputDao,
    private val sortedDao: SortedDao,
    private val logger: Logger
) : InputTaskRepository {


    override val listTaskFlow: Flow<List<TaskInputHandled>> get() = inputDao.getTasksFlow()

    override fun sortedAll(sortField: SortField, sortType: SortType): Flow<List<TaskInputHandled>> =
        sortedDao.getSortedTasks(GetSortInt(sortType, sortField))

    override fun filteredIdo(
        myId: String,
        sortField: SortField,
        sortType: SortType
    ): Flow<List<TaskInputHandled>> = sortedDao.getTasksIDo(myId, GetSortInt(sortType, sortField))

    override fun filteredIDelegate(
        myId: String,
        sortField: SortField,
        sortType: SortType
    ): Flow<List<TaskInputHandled>> = sortedDao.getTasksIDelegate(myId, GetSortInt(sortType, sortField))

    override fun filteredIDidNtCheck(
        myId: String,
        sortField: SortField,
        sortType: SortType
    ): Flow<List<TaskInputHandled>> = sortedDao.getTasksIDidNtCheck(myId, GetSortInt(sortType, sortField))

    override fun filteredIObserve(
        myId: String,
        sortField: SortField,
        sortType: SortType
    ): Flow<List<TaskInputHandled>> = sortedDao.getTasksIObserve(myId, GetSortInt(sortType, sortField))

    override fun filteredIDidNtRead(
        sortField: SortField,
        sortType: SortType
    ): Flow<List<TaskInputHandled>> = sortedDao.getTasksIDidNtRead(GetSortInt(sortType, sortField))

    override suspend fun getTasks(): List<TaskInputHandled> = inputDao.getTasks()

    override fun getTaskFlow(taskId: String): Flow<TaskInputHandled?> = inputDao.getTaskFlow(taskId)

    override suspend fun getTask(taskId: String): TaskInputHandled? = inputDao.getTask(taskId)

    // todo add delete taskDomains not in input list
    override suspend fun insertTasks(taskInputList: List<TaskInput>, whoAmI: UserInput) {
        logger.log(TAG, "Count taskInputList: ${taskInputList.size}")
        var saveCounter = 0
        taskInputList.forEach {
            saveCounter += insertTask(it, whoAmI)
        }
        logger.log(TAG, "Count saved inputTasks: $saveCounter")
    }

    //
//    override suspend fun getTasks(): List<TaskInputHandled> = extraDao.taskInAndExtraList()
//
    private suspend fun insertTask(taskInput: TaskInput, whoAmI: UserInput): Int {
        // prepare taskIn
        val taskIn = inputDao.getTask(taskInput.id)
        // compare with current and save if diff
        taskIn?.let {
            if (taskInput != it.taskIn) {
                inputDao.insertTask(
                    taskInput.toTaskInputHandled(whoAmI)
                )
//            logger.log(TAG, "update taskInput: ${taskInput.toString().replace(", ", "\n")}")
                return 1
            }
        } ?: kotlin.run {
            inputDao.insertTask(
                taskInput.toTaskInputHandled(whoAmI)
            )
//            logger.log(TAG, "saved new taskInput: ${taskInput.toString().replace(", ", "\n")}")
            return 1
        }
        return 0
    }

    override suspend fun updateReading(taskId: String, unread: Boolean) {
        inputDao.getTask(taskId)?.let {
            if (it.unread != unread) {
                inputDao.updateReadingState(taskId, unread)
            }
        }
    }

    override suspend fun saveAndDelete(
        inputTask: TaskInput,
        outputTask: OutputTask,
        whoAmI: UserInput
    ) {
        inputDao.saveAndDelete(
            inputTask,
            outputTask,
            inputTask.toTaskInputHandled(whoAmI)
        )
    }

    override val listUsersFlow: Flow<List<UserInput>> get() = inputDao.getUsersFlow()

    override suspend fun getUser(userId: String): UserInput? = inputDao.getUser(userId)

    override suspend fun getUsers(): List<UserInput> = inputDao.getUsers()

    override suspend fun insertUsers(userInputList: List<UserInput>) {
        userInputList.forEach {
            insertUser(it)
        }
    }

    private suspend fun insertUser(userInput: UserInput) {
        val currentVersionUser = getUser(userInput.id)
        if (userInput != currentVersionUser) {
            inputDao.insertUser(userInput)
        }
    }

}