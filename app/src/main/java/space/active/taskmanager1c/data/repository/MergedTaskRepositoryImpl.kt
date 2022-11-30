package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput.Companion.mapAndReplaceById
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask.Companion.toListTaskInput
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.repository.TasksRepository

class MergedTaskRepositoryImpl(
    inputTaskRepository: InputTaskRepository,
    outputTaskRepository: OutputTaskRepository
) : TasksRepository {
    override val listTasks: Flow<Request<List<Task>>> =
        combine(inputTaskRepository.listTasks, outputTaskRepository.outputTask) { input, output ->
            combineListTasks(input, output)
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

    private fun combineListTasks(taskIn: Request<List<TaskInput>>, taskOut: Request<List<OutputTask>>): Request<List<Task>> {
        // actuality of input tasks
        // итоговое отображение актуальных задач формируется из исходящих и входящих задач.
        // Исходящие изменения должны быть отправлены и подтверждены в виде ответа от сервера с итоговым видом измененной задачи.
        // Ответ от сервера записываем в таблицу входящих. И показываем обновленный список
        // Процесс обновления при отправке:
        // - проверка на соответствие изменений во входящих задачах.
        // - Если изменений нет:
        //      -отправка исходящих задач,
        //      -получение ответа с изменениями,
        //      -обновление списка входящих задач.
        // Процесс регулярного обновления: получение актуального списка задач с определенной периодичностью.
        // Процесс объединения:
        // - берем входящие задачи, берем исходящие задачи. И сравниваем их по отличиям в параметрах.
        //      Если отличий нет, удаляем исходящую задачу.
        //      Иначе объединяем параметры в итоговый список.

        if (taskIn is SuccessRequest && taskOut is SuccessRequest) {
            /**
             * Take OutputTask and find the same in InputTasks.
             * Replace changed and not sended in InputTasks from OutputTask
             * Add new tasks from OutputTask to InputTasks
             */
            if (taskOut.data.isNotEmpty()) {
                val convertedTaskOutput = taskOut.data.toListTaskInput()
                val replacedList = taskIn.data.mapAndReplaceById(convertedTaskOutput)
                val finalList = replacedList.addNotContained(convertedTaskOutput)
                return SuccessRequest(Task.fromTaskInputList(finalList))
            } else {
                return SuccessRequest(Task.fromTaskInputList(taskIn.data))
            }
        } else{
            if (taskIn is ErrorRequest) {
                return ErrorRequest(taskIn.exception)
            }
            if (taskOut is ErrorRequest) {
                return ErrorRequest(taskOut.exception)
            }
        }
        return PendingRequest()
    }
}