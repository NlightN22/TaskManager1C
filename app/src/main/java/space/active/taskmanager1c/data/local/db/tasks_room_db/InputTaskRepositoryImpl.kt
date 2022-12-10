package space.active.taskmanager1c.data.local.db.tasks_room_db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskAndMessages
import space.active.taskmanager1c.data.repository.InputTaskRepository

class InputTaskRepositoryImpl(
    private val inputDao: TaskInputDao
) : InputTaskRepository {



    override val listTaskFlow: Flow<List<TaskInput>> get() = inputDao.getTasksFlow()

    override val listTasksRequest: Flow<Request<List<TaskInput>>> =
        inputDao.getTasksFlow().map { listTask ->
            if (listTask.isNotEmpty()) {
                SuccessRequest(listTask)
            } else {
                ErrorRequest(EmptyObject)
            }
        }

    override fun getTaskAndMessages(taskId: String): Flow<TaskAndMessages> =
        inputDao.getTaskAndMessages(taskId)

    override fun getTaskFlow(taskId: String): Flow<TaskInput?> = inputDao.getTaskFlow(taskId)

    override suspend fun getTask(taskId: String): TaskInput? = inputDao.getTask(taskId)

    override suspend fun getTasks(): List<TaskInput> = inputDao.getTasks()

    override suspend fun insertTask(taskInput: TaskInput) {
        val currentVersionTask = getTask(taskInput.id)
        if (taskInput != currentVersionTask) {
            inputDao.insertTask(taskInput)
        }
    }

    override suspend fun insertTasks(taskInputList: List<TaskInput>) {
        taskInputList.forEach {
            insertTask(it)
        }
    }

    override val listUsersFlow: Flow<List<UserInput>> = inputDao.getUsersFlow()

    override suspend fun getUser(userId: String): UserInput? = inputDao.getUser(userId)

    override suspend fun insertUser(userInput: UserInput) {
        val currentVersionUser = getUser(userInput.id)
        if (userInput != currentVersionUser) {
            inputDao.insertUser(userInput)
        }
    }

    override suspend fun insertUsers(userInputList: List<UserInput>) {
        userInputList.forEach {
            insertUser(it)
        }
    }
}