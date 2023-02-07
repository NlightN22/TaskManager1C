package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.ReadingTimesTaskEntity
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskInputHandledWithUsers
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "InputTaskRepositoryImpl"

@Singleton
class InputTaskRepositoryImpl @Inject constructor(
    private val inputDao: TaskInputDao,
    private val readingDao: TaskReadingDao,
    private val logger: Logger
) : InputTaskRepository {

    override suspend fun getInputTasksCount(): Int = inputDao.getInputCount()

    override fun sortedQuery(
        myId: String,
        filterType: FilterType,
        sortField: SortField,
        sortType: SortType
    ): Flow<List<TaskInputHandledWithUsers>> {
        val query = GetSort.getFilterSQL(filterType, myId) + GetSort.getFieldSQL(sortField) + GetSort.getSortSQL(sortType)
        logger.log(TAG, "query: $query")
        return inputDao.getSortedTaskQuery(SimpleSQLiteQuery(query))
    }

    override suspend fun getTasks(): List<TaskInputHandledWithUsers> = inputDao.getTasks()

    override fun getTaskFlow(taskId: String): Flow<TaskInputHandledWithUsers?> =
        inputDao.getTaskFlow(taskId)

    override fun getInnerTasks(taskId: String): Flow<List<TaskInputHandledWithUsers>> =
        inputDao.getInnerTasksFlow(taskId)

    override suspend fun getTask(taskId: String): TaskInputHandledWithUsers? =
        inputDao.getTask(taskId)

    override suspend fun insertTasks(
        taskInputList: List<TaskInputHandledWithUsers>
    ) {
        logger.log(TAG, "Count taskInputList: ${taskInputList.size}")
        // delete not coming
        deleteNotComing(taskInputList.map { it.taskInput.id })
        var saveCounter = 0
        // insert changed
        taskInputList.forEach {
            saveCounter += insertTask(it)
        }
        logger.log(TAG, "Count saved inputTasks: $saveCounter")
    }

    private suspend fun deleteNotComing(listIds: List<String>) {
        val notInDbIds = inputDao.getIdsNotInList(listIds)
        logger.log(TAG, "Count tasks to delete: ${notInDbIds.size}")
        notInDbIds.forEach {
            inputDao.deleteTask(it)
        }
    }

    private suspend fun insertTask(taskHandled: TaskInputHandledWithUsers): Int {
        // prepare taskIn
        val taskIn = inputDao.getTask(taskHandled.taskInput.id)
        // compare with current and save if diff
        taskIn?.let { current ->
            if (taskHandled.taskInput.version > current.taskInput.version) {
                inputDao.insertTask(
                    taskHandled
                )
//            logger.log(TAG, "update taskInput: ${taskInput.toString().replace(", ", "\n")}")
                return 1
            }
        } ?: kotlin.run {
            inputDao.insertTask(
                taskHandled
            )
//            logger.log(TAG, "saved new taskInput: ${taskInput.toString().replace(", ", "\n")}")
            return 1
        }
        return 0
    }

    override suspend fun saveAndDelete(
        inputTask: TaskInputHandledWithUsers,
        outputTask: OutputTask,
        whoAmI: UserInput
    ) {

        val taskIn = inputDao.getTask(inputTask.taskInput.id)
        taskIn?.let { current ->
//            logger.log(TAG, "save new: ${inputTask.taskInput.version} cur: ${current.taskInput.version}")
            if (inputTask.taskInput.version > current.taskInput.version) {
                inputDao.saveAndDelete(
                    inputTask,
                    outputTask,
                )
                return
//            logger.log(TAG, "update taskInput: ${taskInput.toString().replace(", ", "\n")}")
            }
        }
        inputDao.deleteOutputTask(outputTask.outputId)
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
        val currentVersionUser = getUser(userInput.userId)
        if (userInput != currentVersionUser) {
            inputDao.insertUser(userInput)
        }
    }

    override suspend fun updateReadingStates(readingTimes: List<ReadingTimesTaskEntity>) {
        logger.log(TAG, "Count ReadingStates: ${readingTimes.size}")
        var saveCounter = 0
        readingTimes.forEach { item ->
            val current = readingDao.getReading(item.mainTaskId)
            current?.let {
                if (it != item) {
                    readingDao.insertReading(item)
                    saveCounter += 1
                }
            } ?: kotlin.run {
                readingDao.insertReading(item)
                saveCounter += 1
            }
        }
        logger.log(TAG, "Count saved ReadingStates: $saveCounter")
    }

    override fun getUnreadIds(): Flow<List<String>> = readingDao.getUnreadIds()

    override suspend fun setUnreadTag(taskId: String, version: Int, unreadTag: Boolean) {
        inputDao.updateUnreadTag(taskId, version, unreadTag)
    }
}