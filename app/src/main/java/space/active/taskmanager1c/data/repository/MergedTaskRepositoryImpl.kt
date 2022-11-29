package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.repository.TasksRepository

class MergedTaskRepositoryImpl(
    val inputTask: InputTaskRepository,
    val outputTask: OutputTaskRepository
): TasksRepository {
    override val listTasks: Flow<Request<List<Task>>>
        get() = TODO("Not yet implemented")

    override fun getTask(taskId: String): Flow<Request<Task>> {
        TODO("Not yet implemented")
    }

    override fun editTask(task: Task): Flow<Request<Any>> {
        TODO("Not yet implemented")
    }

    override fun createNewTask(task: Task): Flow<Request<Any>> {
        TODO("Not yet implemented")
    }

    override fun attachFileToTask(file: ByteArray, taskId: String): Flow<Request<Any>> {
        TODO("Not yet implemented")
    }

    private fun mergeTask(taskIn: Task, taskOut: Task) : Task  {
        TODO("merge input and not sending output tasks")
    }
}