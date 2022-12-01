package space.active.taskmanager1c.data.local.db.tasks_room_db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.repository.OutputTaskRepository

class OutputTaskRepositoryImpl(
    private val taskOutputDao: TaskOutputDao
): OutputTaskRepository {
    override val outputTask: Flow<List<OutputTask>> = taskOutputDao.getOutputTasksFlow()
    //    override val outputTask: Flow<Request<List<OutputTask>>> = taskOutputDao.getOutputTasksFlow().map { listOutput ->
//        if (listOutput.isNotEmpty()) {
//            SuccessRequest(listOutput)
//        } else {
//            ErrorRequest(EmptyObject)
//        }
//    }

    override suspend fun insertTask(outputTask: OutputTask) = taskOutputDao.insertTask(outputTask)

    override suspend fun getTasks(): List<OutputTask> = taskOutputDao.getOutputTasks()

    override suspend fun deleteTasks(outputTasks: List<OutputTask>) {
        outputTasks.forEach {
            taskOutputDao.deleteOutputTask(it.outputId)
        }
    }
}