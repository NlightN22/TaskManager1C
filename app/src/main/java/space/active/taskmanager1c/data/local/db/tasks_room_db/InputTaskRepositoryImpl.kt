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

    override suspend fun getTask(taskId: String): TaskInAndExtra? = extraDao.getTaskInAndExtra(taskId)

    override suspend fun getTasks(): List<TaskInAndExtra> = extraDao.taskInAndExtraList()

    override suspend fun insertTask(taskInput: TaskInput, whoAmI: UserInput): Int {
        // prepare taskIn
        val taskIn = extraDao.getInput(taskInput.id)
        // compare with current and save if diff
        val saveIn: Boolean = taskIn?.let {
            taskInput != it
        } ?: true
        // prepare taskExtra
        val taskEx = extraDao.getExtra(taskInput.id)
        // compare with current and save if diff
        val saveEx: Boolean = taskEx?.let {
            it.insertFromInput(taskInput, whoAmI) != it
        } ?: true

        if (saveIn || saveEx) {
            extraDao.insertTaskInAndExtra(taskInput.toTaskExtra(whoAmI), taskInput)
            return 1
        }
        return 0
    }

    override suspend fun insertTasks(taskInputList: List<TaskInput>, whoAmI: UserInput) {
        var saveCounter = 0
        taskInputList.forEach {
            saveCounter += insertTask(it, whoAmI)
        }
        logger.log(TAG, "Count saved tasks: $saveCounter")
    }

    override suspend fun updateReading(taskId: String, unread: Boolean) {
        extraDao.getTaskExtra(taskId)?.let {
            if (it.unread != unread) {
                extraDao.updateIsReading(taskId, unread)
            }
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