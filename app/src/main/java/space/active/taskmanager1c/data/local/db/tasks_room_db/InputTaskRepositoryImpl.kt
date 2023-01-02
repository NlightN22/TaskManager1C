package space.active.taskmanager1c.data.local.db.tasks_room_db

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.Label
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.LabelWithTasks
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.TaskExtraLabelCrossRef
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.TaskInAndExtra
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.TaskWithLabels

private const val TAG = "InputTaskRepositoryImpl"

class InputTaskRepositoryImpl(
    private val inputDao: TaskInputDao,
    private val extraDao: TaskExtraInputDao,
    private val logger: Logger
) : InputTaskRepository {


    override val listTaskFlow: Flow<List<TaskInAndExtra>> get() = extraDao.taskInAndExtraListFlow()

    override fun getTaskFlow(taskId: String): Flow<TaskInAndExtra?> = extraDao.getTaskFlow(taskId)

    override suspend fun getTask(taskId: String): TaskInAndExtra? = extraDao.getTaskExtra(taskId)

    override suspend fun getTasks(): List<TaskInAndExtra> = extraDao.taskInAndExtraList()

    override suspend fun insertTask(taskInput: TaskInput, whoAmI: UserInput) {
//        logger.log(TAG, "insertTask: ${taskInput.name}")
        val taskExtra: TaskInAndExtra = taskInput.toTaskExtra(whoAmI)
//        logger.log(TAG, "try to get in DB")
        val currentVersionTask: TaskInAndExtra? = getTask(taskInput.id)
        try {

            currentVersionTask?.let {
                if (taskExtra != it) {
                    extraDao.insertTaskInAndExtra(taskExtra)
//                    logger.log(TAG, "try to save: ${taskExtra.taskIn.id}")
//                    extraDao.insertTaskInput(taskExtra.taskIn)
//                    logger.log(TAG, "try to save: ${taskExtra.extra.taskId}")
//                    extraDao.insertTaskExtra(taskExtra.extra)
                }
            } ?: kotlin.run {
                extraDao.insertTaskInAndExtra(taskExtra)
//                logger.log(TAG, "try to save: ${taskExtra.taskIn.id}")
//                extraDao.insertTaskInput(taskExtra.taskIn)
//                logger.log(TAG, "try to save: ${taskExtra.extra.taskId}")
//                extraDao.insertTaskExtra(taskExtra.extra)
            }
        } catch (e: Throwable) {
            logger.log(TAG, "$taskExtra")
        }
    }

    override suspend fun insertTasks(taskInputList: List<TaskInput>, whoAmI: UserInput) {
        taskInputList.forEach {
            insertTask(it, whoAmI)
        }
    }

    override val listUsersFlow: Flow<List<UserInput>> get() = inputDao.getUsersFlow()

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

    override fun taskWithLabels(taskId: String): Flow<TaskWithLabels?> = extraDao.getTaskWithLabels(taskId)

    override fun labelWithTasks(label: Label): Flow<LabelWithTasks?> = extraDao.getLabelWithTasks(label.labelName)

    override suspend fun insertLabel(taskId: String, label: Label) {
        extraDao.insertLabel(TaskExtraLabelCrossRef(taskId, label.labelName))
    }
}