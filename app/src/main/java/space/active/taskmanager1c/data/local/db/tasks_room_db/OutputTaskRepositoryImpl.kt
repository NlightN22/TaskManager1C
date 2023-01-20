package space.active.taskmanager1c.data.local.db.tasks_room_db

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.local.OutputTaskRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "OutputTaskRepositoryImpl"

@Singleton
class OutputTaskRepositoryImpl @Inject constructor(
    private val taskOutputDao: TaskOutputDao,
    private val logger: Logger
) : OutputTaskRepository {
    override val outputTaskList: Flow<List<OutputTask>> = taskOutputDao.getOutputTasksFlow()

    override suspend fun insertTask(outputTask: OutputTask)  {
        /**
         * check for the same taskDomain in table by input taskId
         */
        val existingInTable = getTask(outputTask.taskInput.id)
        existingInTable?.let {
            /**
             *  check the same without output taskDomain fields
             */
            val incomeWithoutId = outputTask.copy(outputId = 0)
            val existingWithoutId = it.copy(outputId = 0)
            if (incomeWithoutId == existingWithoutId) {
                logger.error(
                    TAG,
                    "fun insertTask find existing taskDomain ${outputTask.taskInput.id} in table"
                )
            } else {
                taskOutputDao.insertTask(outputTask.copy(outputId = it.outputId))
            }
        } ?: kotlin.run {
            taskOutputDao.insertTask(outputTask)
        }
    }

    override suspend fun getTasks(): List<OutputTask> = taskOutputDao.getOutputTasks()

    override fun getTaskFlow(taskInputId: String): Flow<OutputTask?> =
        taskOutputDao.getOutputTaskFlow(taskInputId)

    override suspend fun getTask(taskInputId: String): OutputTask? =
        taskOutputDao.getOutputTask(taskInputId)

    override suspend fun deleteTasks(outputTasks: List<OutputTask>) {
        outputTasks.forEach {
            taskOutputDao.deleteOutputTask(it.outputId)
        }
    }

    override suspend fun deleteTask(outputTask: OutputTask) {
        taskOutputDao.deleteOutputTask(outputTask.outputId)
    }

}