package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.compareWithAndGetDiffs
import space.active.taskmanager1c.data.repository.InputTaskRepository
import space.active.taskmanager1c.data.repository.OutputTaskRepository
import space.active.taskmanager1c.data.repository.TaskApi
import space.active.taskmanager1c.di.IoDispatcher
import javax.inject.Inject

private const val TAG = "HandleJobForUpdateDb"


class HandleJobForUpdateDb
@Inject constructor(
    private val inputTaskRepository: InputTaskRepository,
    private val outputTaskRepository: OutputTaskRepository,
    private val taskApi: TaskApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger,
) {
    fun updateJob(updateDelay: Long = 1000L): Flow<Request<Any>> = flow {
        // Проверяем таблицу исходящих задач. С определенной переодичностью
        // Если в таблице исходящих есть задачи, то сравниваем их с таблицей входящих задач.
        // При совпадении исходящих и входящих задач удаляем совпадающие из таблицы исходящих.
        // Оставшиеся исходящие задачи отправляем на сервер.
        // При успешной отправке на сервер:
        //  в качестве подтверждения получаем входящую задачу с актуальными значениями
        //  записываем задачу во входящие задачи и удаляем из исходящих отправленную
        // Далее запрашиваем с сервера входящие задачи. С определенной периодичностью.
        // Обновляем список входящих задач в таблице.
        // Дополнительные параметры - попытки отправки на сервер, таймауты отправки на сервер. Исключения при их достижении
        while (true) {
            logger.log(TAG, "Job start")
            emit(outputSendJob())
            emit(inputFetchJob())
            delay(updateDelay)
            logger.log(TAG, "Job end")
        }
    }.flowOn(ioDispatcher)

    private suspend fun outputSendJob(): Request<Any> {
        val outputTasks: List<OutputTask> = outputTaskRepository.getTasks()
        val inputTasks: List<TaskInput> = inputTaskRepository.getTasks()
        if (outputTasks.isNotEmpty()) {
            // find an delete the same tasks in output and input table
            val deleteOutputTaskList: List<OutputTask> =
                getEqualOutputTasksInInputTasks(outputTasks, inputTasks)
            outputTaskRepository.deleteTasks(deleteOutputTaskList)
            // get only not deleted output tasks
            val outputTasksAfterDelete: List<OutputTask> = outputTaskRepository.getTasks()
            outputTasksAfterDelete.forEach { outputTask ->
                // prepare to send changes
                val existingInputTask = inputTaskRepository.getTask(outputTask.taskInput.id)
                var result: Request<TaskDto>
                // convert to DTO
                val outToDTO = TaskDto.fromOutputTask(outputTask)
                // find diffs with TaskInput if id in input table or send as is
                if (existingInputTask != null) {
                    result = ErrorRequest<TaskDto>(ThisTaskIsNotEdited)
                    // if task is not edited and existing in input table
                    if (!outputTask.newTask) {
                        // get taskDTO with id and only diffs params
                        val outDTOWithoutId = outToDTO.copy(id = "")
                        // get diff map key value
                        val inputDTO = TaskDto.fromInputTask(existingInputTask)
                        val mappedDiffs = inputDTO.compareWithAndGetDiffs(outDTOWithoutId)
                        // send in Map
                        result = taskApi.sendEditedTaskMappedChanges(mappedDiffs)
                    }
                } else {
                    // if task is not new
                    result = ErrorRequest<TaskDto>(ThisTaskIsNotNew)
                    // clear id for new tasks
                    if (outputTask.newTask) {
                        val withoutId = outToDTO.copy(id = "")
                        // send all params
                        result = taskApi.sendNewTask(withoutId)
                    }
                }
                when (result) {
                    is SuccessRequest -> {
                        // TODO if task send with delete label
                        inputTaskRepository.insertTask(result.data.toTaskInput())
                        outputTaskRepository.deleteTasks(listOf(outputTask))
                        return SuccessRequest(Any())
                    }
                    is ErrorRequest -> {
                        return ErrorRequest(result.exception)
                    }
                    is PendingRequest -> {
                        return PendingRequest()
                    }
                }
            }
        }
        return SuccessRequest(Any())
    }

    suspend fun inputFetchJob(): Request<Any> {
        val result = taskApi.getTaskList()
        when (result) {
            is SuccessRequest -> {
                inputTaskRepository.insertTasks(result.data.toTaskInputList())
                return SuccessRequest(Any())
            }
            is ErrorRequest -> {
                return ErrorRequest(result.exception)
            }
            is PendingRequest -> {
                return PendingRequest()
            }
        }
    }

    fun inputFetchJobFlow() = flow<Request<Any>> {
        taskApi.getTaskListFlow().collect { request ->
            when (request) {
                is SuccessRequest -> {
                    inputTaskRepository.insertTasks(request.data.toTaskInputList())
                    emit(SuccessRequest(Any()))
                }
                is ErrorRequest -> {
                    emit(ErrorRequest(request.exception))
                }
                is PendingRequest -> {
                    emit(PendingRequest())
                }
            }
        }
    }.flowOn(ioDispatcher)

    private fun getEqualOutputTasksInInputTasks(
        outputTasks: List<OutputTask>,
        inputTasks: List<TaskInput>
    ): List<OutputTask> {
        val listToDelete = arrayListOf<OutputTask>()

        outputTasks.forEach { outputTask ->
            if (inputTasks.contains(outputTask.taskInput)) {
                listToDelete.add(outputTask)
            }
        }
        return listToDelete
    }
}