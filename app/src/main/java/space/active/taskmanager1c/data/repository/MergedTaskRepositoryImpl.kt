package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.addNotContained
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput.Companion.mapAndReplaceById
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask.Companion.toListTaskInput
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.repository.TasksRepository

private const val TAG = "MergedTaskRepositoryImpl"

class MergedTaskRepositoryImpl(
    private val inputTaskRepository: InputTaskRepository,
    private val outputTaskRepository: OutputTaskRepository,
    private val logger: Logger
) : TasksRepository {

    override val listTasksFlow: Flow<List<Task>> =
        combine(
            inputTaskRepository.listTaskFlow,
            outputTaskRepository.outputTask
        ) { inputList, outputList ->
            combineListTasks(inputList, outputList)
        }


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

    private fun combineListTasks(
        taskIn: List<TaskInput>,
        taskOut: List<OutputTask>
    ): List<Task> {
        if (taskIn.isNotEmpty()) {
            /**
             * Take OutputTask and find the same in InputTasks.
             * Replace changed and not sended in InputTasks from OutputTask
             * Add new tasks from OutputTask to InputTasks
             */
            if (taskOut.isNotEmpty()) {
                val convertedTaskOutput = taskOut.toListTaskInput()
                val replacedList = taskIn.mapAndReplaceById(convertedTaskOutput)
                val finalList = replacedList.addNotContained(convertedTaskOutput)
                return Task.fromTaskInputList(finalList)
            } else {
                return Task.fromTaskInputList(taskIn)
            }
        } else {
            return emptyList()
        }
    }
}