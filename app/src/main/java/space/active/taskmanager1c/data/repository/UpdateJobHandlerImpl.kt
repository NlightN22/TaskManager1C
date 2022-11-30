package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.domain.repository.UpdateJobHandler

class UpdateJobHandlerImpl(
    private val inputTaskRepository: InputTaskRepository,
    private val outputTaskRepository: OutputTaskRepository,
    private val taskApi: TaskApi
) : UpdateJobHandler {

//    private val inputTasks: Flow<Request<List<TaskInput>>> = inputTaskRepository.listTasks
//    private val outputTask: Flow<Request<List<OutputTask>>> = outputTaskRepository.outputTask

    // TODO add IO Dispatcher to data layer
    // TODO add jobs class for emit request

    override fun updateJob(coroutineScope: CoroutineScope): Flow<Request<Any>> = flow {
        // Проверяем таблицу исходящих задач. С определенной переодичностью
        // Если в таблице исходящих есть задачи, то сравниваем их с таблицей входящих задач.
        // При совпадении исходящих и входящих задач удаляем совпадающие из таблицы исходящих.
        // Оставшиеся исходящие задачи отправляем на сервер.
        // При успешной отправке на сервер:
        //  в качестве подтверждения получаем входящую задачу с актуальными значениями
        //  записываем задачу во входящие задачи и удаляем из исходящих отправленную
        // Далее запрашиваем с сервера входящие задачи. С определенной периодичностью.
        // Обновляем список входящих задач в таблице.
        // Дополнительные параметры - попытки отправки на сервер, таймауты отправки на сервер
        while (true) {
            emit(outputSendJob())
            emit(inputFetchJob())
            delay(100) // TODO add CONST DELAY
        }
    }

    private suspend fun outputSendJob(): Request<Any> {
        val outputTasks: List<OutputTask> = outputTaskRepository.getTasks()
        val inputTasks: List<TaskInput> = inputTaskRepository.getTasks()
        if (outputTasks.isNotEmpty()) {
            val deleteOutputTaskList: List<OutputTask> =
                getEqualOutputTasksInInputTasks(outputTasks, inputTasks)
            outputTaskRepository.deleteTasks(deleteOutputTaskList)
            val outputTasksAfterDelete: List<OutputTask> = outputTaskRepository.getTasks()
            outputTasksAfterDelete.forEach { outputTask ->
                val result = taskApi.sendTaskChanges(TaskDto.fromOutputTask(outputTask))
                when (result) {
                    is SuccessRequest -> {
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
        return PendingRequest()
    }

    private suspend fun inputFetchJob(): Request<Any> {
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