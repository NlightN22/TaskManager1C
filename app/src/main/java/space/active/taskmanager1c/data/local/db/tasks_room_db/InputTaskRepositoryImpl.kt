package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskInputHandledWithUsers
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "InputTaskRepositoryImpl"

@Singleton
class InputTaskRepositoryImpl @Inject constructor(
    private val inputDao: TaskInputDao,
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

    override suspend fun getTask(taskId: String): TaskInputHandledWithUsers? =
        inputDao.getTask(taskId)

    // todo add delete taskDomains not in input list
    override suspend fun insertTasks(
        taskInputList: List<TaskInputHandledWithUsers>
    ) {
        logger.log(TAG, "Count taskInputList: ${taskInputList.size}")
        var saveCounter = 0
        taskInputList.forEach {
            saveCounter += insertTask(it)
        }
        logger.log(TAG, "Count saved inputTasks: $saveCounter")
    }

    private suspend fun insertTask(taskHandled: TaskInputHandledWithUsers): Int {
        // prepare taskIn
        val taskIn = inputDao.getTask(taskHandled.taskInput.id)
        // compare with current and save if diff
        taskIn?.let {
            if (taskHandled != it) {
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

    // todo delete
//    override suspend fun updateReading(taskId: String, unread: Boolean) {
//        inputDao.getTask(taskId)?.let {
//            if (it.unread != unread) {
//                inputDao.updateReadingState(taskId, unread)
//            }
//        }
//    }

    override suspend fun saveAndDelete(
        inputTask: TaskInputHandledWithUsers,
        outputTask: OutputTask,
        whoAmI: UserInput
    ) {
        inputDao.saveAndDelete(
            inputTask,
            outputTask,
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
        val currentVersionUser = getUser(userInput.userId)
        if (userInput != currentVersionUser) {
            inputDao.insertUser(userInput)
        }
    }

}