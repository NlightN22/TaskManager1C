package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.OutputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.remote.TaskApi
import space.active.taskmanager1c.data.remote.dto.compareWithAndGetDiffs
import space.active.taskmanager1c.data.remote.model.TaskDto
import space.active.taskmanager1c.data.remote.model.TaskListDto
import space.active.taskmanager1c.data.remote.model.reading_times.ReadingTimesTask
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Credentials
import space.active.taskmanager1c.domain.repository.UpdateJobInterface
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val TAG = "UpdateJobInterfaceImpl"


class UpdateJobInterfaceImpl
@Inject constructor(
    private val inputTaskRepository: InputTaskRepository,
    private val outputTaskRepository: OutputTaskRepository,
    private val taskApi: TaskApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger,
) : UpdateJobInterface {

    override fun updateJob(credentials: Credentials, updateDelay: Long, whoAmI: UserInput): Flow<Request<Any>> =
        channelFlow<Request<Any>> {
            // Проверяем таблицу исходящих задач. С определенной переодичностью
            // Если в таблице исходящих есть задачи, то сравниваем их с таблицей входящих задач.
            // При совпадении исходящих и входящих задач удаляем совпадающие из таблицы исходящих.
            // Оставшиеся исходящие задачи отправляем на сервер.
            // При успешной отправке на сервер:
            //  в качестве подтверждения получаем входящую задачу с актуальными значениями
            //  записываем задачу во входящие задачи и удаляем из исходящих отправленную
            // Далее запрашиваем с сервера входящие задачи.
            // Обновляем список входящих задач в таблице.
            // Дополнительные параметры - попытки отправки на сервер, таймауты отправки на сервер. Исключения при их достижении
            var curTime = System.currentTimeMillis()
            logger.log(TAG, "outputSendJob start")
            outputSendJob(credentials, whoAmI).collect {
                send(it)
            }
            logger.log(TAG, "outputSendJob stop ${System.currentTimeMillis() - curTime}ms")

            curTime = System.currentTimeMillis()
            logger.log(TAG, "inputFetchJobFlow start")
            inputFetchJobFlow(credentials, whoAmI).collect {
                send(it)
            }
            logger.log(TAG, "inputFetchJobFlow stop ${System.currentTimeMillis() - curTime}ms")

            curTime = System.currentTimeMillis()
            logger.log(TAG, "updateReadingState start")
            updateReadingState(credentials).collect{
                send(it)
            }
            logger.log(TAG, "updateReadingState stop ${System.currentTimeMillis() - curTime}ms")

            delay(updateDelay)
//            logger.log(TAG, "Job end")
        }.flowOn(ioDispatcher)

    private fun updateReadingState(credentials: Credentials) = flow<Request<Any>> {
        emit(PendingRequest())
        val result: List<ReadingTimesTask> = taskApi.getMessagesTimes(credentials.toAuthBasicDto(), inputTaskRepository.getTasks().map { it.extra.taskId})
        result.forEach {
            inputTaskRepository.updateReading(it.id, it.getUnreadStatus())
        }
        emit(SuccessRequest(Any()))
    }

    private fun ReadingTimesTask.getUnreadStatus(): Boolean {
        val messageTime = lastMessageTime.toDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val lastRead = readingTime.toDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return (messageTime > lastRead)
    }

    private fun outputSendJob(credentials: Credentials, whoAmI: UserInput) = flow<Request<Any>> {
        val outputTasks: List<OutputTask> = outputTaskRepository.getTasks()
        val inputTasks: List<TaskInput> = inputTaskRepository.getTasks().map { it.taskIn }
        if (outputTasks.isNotEmpty()) {
            // find an delete the same tasks in output and input table
            val deleteOutputTaskList: List<OutputTask> =
                getEqualOutputTasksInInputTasks(outputTasks, inputTasks)
            outputTaskRepository.deleteTasks(deleteOutputTaskList)
            // get only not deleted output tasks
            val outputTasksAfterDelete: List<OutputTask> = outputTaskRepository.getTasks()
            var result: Request<TaskDto>? = null
            outputTasksAfterDelete.forEach { outputTask ->
                // prepare to send changes
                val existingInputTask = inputTaskRepository.getTask(outputTask.taskInput.id)
                // convert to DTO
                val outToDTO = TaskDto.fromOutputTask(outputTask)
                // find diffs with TaskInput if id in input table or send as is
                existingInputTask?.let {
                    result = ErrorRequest<TaskDto>(ThisTaskIsNotEdited(it.taskIn.name))
                    // if task is not edited and existing in input table
                    if (!outputTask.newTask) {
                        // get taskDTO with id and only diffs params
//                        val outDTOWithoutId = outToDTO.copy(id = "")
                        // get diff map key value
                        val inputDTO = TaskDto.fromInputTask(it.taskIn)
//                        val mappedDiffs = inputDTO.compareWithAndGetDiffs(outDTOWithoutId)
                        // send in Map
//                        val res = taskApi.sendEditedTaskMappedChanges(
//                            userSettings.toAuthBasicDto(),
//                            inputDTO.id,
//                            mappedDiffs
//                        )
//                        result = SuccessRequest(res)
                        result = SuccessRequest(
                            taskApi.sendEditedTaskMappedChanges(
                                credentials.toAuthBasicDto(),
                                inputDTO.id,
                                inputDTO.compareWithAndGetDiffs(outToDTO.copy(id = ""))
                            )
                        )
//                        mock todo delete
//                        result = SuccessRequest(inputDTO)
                    }
                } ?: kotlin.run {
                    // if task is not new
                    result = ErrorRequest<TaskDto>(ThisTaskIsNotNew)
                    // clear id for new tasks
                    if (outputTask.newTask) {
                        val withoutId = outToDTO.copy(id = "")
                        // send all params
//                        result = taskApi.sendNewTask(userSettings.toAuthBasicDto(),withoutId)
                        //mock todo delete
                        delay(6000)
                        result = SuccessRequest(withoutId)
                    }
                }
                result?.let { res ->
                    when (res) {
                        is SuccessRequest -> {
                            // TODO if task send with delete label
//                        logger.log(TAG, result.data.toString())
                            inputTaskRepository.insertTask(
                                res.data.toTaskInput(),
                                whoAmI
                            )
                            outputTaskRepository.deleteTasks(listOf(outputTask))
                            emit(SuccessRequest(Any()))
                        }
                        is ErrorRequest -> {
                            emit(ErrorRequest(res.exception))
                        }
                        is PendingRequest -> {
                            emit(PendingRequest())
                        }
                    }
                }
            }
        }
        emit(SuccessRequest(Any()))
    }

    override fun inputFetchJobFlow(credentials: Credentials, whoAmI: UserInput) = flow<Request<Any>> {
        emit(PendingRequest())
        val result: TaskListDto = taskApi.getTaskList(credentials.toAuthBasicDto())
//        when (request) {
//            is SuccessRequest -> {
//                logger.log(TAG, "get tasks from server")
//                val listUsers = request.data.toUserInputList()
//                val listTasks = request.data.toTaskInputList()
                //save input Users
        var curTime = System.currentTimeMillis()
        logger.log(TAG, "insertUsers")
        inputTaskRepository.insertUsers(result.toUserInputList())
        logger.log(TAG, "insertUsers ${System.currentTimeMillis() - curTime}ms")

        //save input Tasks
                //todo delete
//                inputTaskRepository.insertTasks(listTasks) todo delete
//                logger.log(TAG, "Tasks: ${listTasks.map { it.name }.joinToString("\n")  }")
//                logger.log(TAG, "WhoAmI: $whoAmi")
        curTime = System.currentTimeMillis()
        logger.log(TAG, "insertTasks")
        inputTaskRepository.insertTasks(result.toTaskInputList(), whoAmI)
        logger.log(TAG, "insertTasks ${System.currentTimeMillis() - curTime}ms")
//                logger.log(TAG, "save all to DB")
                emit(SuccessRequest(Any()))
//            }
//            is ErrorRequest -> {
//                emit(ErrorRequest(request.exception))
//            }
//            is PendingRequest -> {
//                emit(PendingRequest())
//            }
//        }
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