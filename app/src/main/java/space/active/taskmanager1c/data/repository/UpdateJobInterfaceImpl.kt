package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.OutputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.remote.TaskApi
import space.active.taskmanager1c.data.remote.dto.compareWithAndGetDiffs
import space.active.taskmanager1c.data.remote.model.TaskDto
import space.active.taskmanager1c.data.remote.model.TaskListDto
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Credentials
import space.active.taskmanager1c.domain.repository.UpdateJobInterface
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

    private val enabledJobLog: Boolean = false

    override fun updateJob(
        credentials: Credentials,
        updateDelay: Long,
        whoAmI: UserInput,
        skippedExceptions: Flow<List<Throwable>>
    ): Flow<Request<Any>> =
        channelFlow<Request<Any>> {
            var curTime = System.currentTimeMillis()
            if (enabledJobLog) logger.log(TAG, "outputSendJob start")
            wrapToSkipExceptions(skippedExceptions) {
                outputSendJob(credentials, whoAmI).collect {
                    send(it)
                }
            }
            if (enabledJobLog) logger.log(
                TAG,
                "outputSendJob stop ${System.currentTimeMillis() - curTime}ms"
            )
            curTime = System.currentTimeMillis()
            if (enabledJobLog) logger.log(TAG, "inputFetchJobFlow start")
            inputFetchJobFlow(credentials, whoAmI).collect { request ->
                when (request) {
                    is SuccessRequest -> {
                        send(SuccessRequest(Any()))
                        if (enabledJobLog) logger.log(
                            TAG,
                            "inputFetchJobFlow stop ${System.currentTimeMillis() - curTime}ms"
                        )
                        val resultListIDs = request.data
                        wrapToSkipExceptions(skippedExceptions) {
                            updateReadingState(credentials, resultListIDs).collect {
                                send(it)
                            }
                        }
                    }
                    is PendingRequest -> send(PendingRequest())
                    is ErrorRequest -> send(ErrorRequest(request.exception))
                }
            }
            delay(updateDelay)
        }.flowOn(ioDispatcher)

    private suspend fun wrapToSkipExceptions(
        skippedException: Flow<List<Throwable>>,
        block: suspend () -> Unit
    ) {
        try {
            block()
        } catch (e: Throwable) {
            if (skippedException.first().contains(e)) {
                logger.log(TAG, "skippedException ${e.toString()}")
                e.printStackTrace()
            } else {
                throw e
            }
        }
    }


    private fun updateReadingState(credentials: Credentials, currentListId: List<String>) =
        flow<Request<Any>> {
            val curTime = System.currentTimeMillis()
            if (enabledJobLog) logger.log(TAG, "updateReadingState start")
            emit(PendingRequest())
            val result = taskApi.getMessagesTimes(
                credentials.toAuthBasicDto(),
                currentListId
            )
            try {
                val readingTimes = result.map { it.toReadingTimesTaskEntity() }
                if (enabledJobLog) logger.log(TAG, "Count ReadingStates: ${readingTimes.size}")
                val saveCounter = inputTaskRepository.updateReadingStates(readingTimes)
                if (enabledJobLog) logger.log(TAG, "Count saved ReadingStates: $saveCounter")
                emit(SuccessRequest(Any()))
                if (enabledJobLog) logger.log(
                    TAG,
                    "updateReadingState stop ${System.currentTimeMillis() - curTime}ms"
                )
            } catch (e: NullPointerException) {
                throw BackendException(
                    errorBody = "${e.message?.toString()} ${result.toString()}",
                    errorCode = "",
                    sendToServerData = Pair(credentials.toAuthBasicDto().name, currentListId)
                )
            }
        }

    private fun outputSendJob(credentials: Credentials, whoAmI: UserInput) = flow<Request<Any>> {
        val outputTasks: List<TaskDto> = outputTaskRepository.getTasks().map { it.taskDto }
        val inputTasks: List<TaskDto> = inputTaskRepository.getTasks().map { it.toTaskDTO() }
        if (outputTasks.isNotEmpty()) {
            // find an delete the same taskDomains in output and input table
            val deleteOutputTaskList: List<String> =
                getEqualOutputTasksInInputTasks(outputTasks, inputTasks)
            outputTaskRepository.deleteTasks(deleteOutputTaskList)
            // get only not deleted output taskDomains
            val outputTasksAfterDelete: List<OutputTask> = outputTaskRepository.getTasks()
            var result: Request<TaskDto>? = null
            outputTasksAfterDelete.forEach { outputTask ->
                // prepare to send changes
                val existingInputTask: TaskDto? =
                    inputTaskRepository.getTask(outputTask.taskDto.id)?.toTaskDTO()
                // convert to DTO
                val outToDTO: TaskDto = outputTask.taskDto
                // find diffs with TaskInput if id in input table or send as is
                existingInputTask?.let { existingInput ->
                    result = ErrorRequest<TaskDto>(ThisTaskIsNotEdited(existingInput.name))
                    // if taskDomain is not edited and existing in input table
                    if (!outputTask.newTask) {
                        result = SuccessRequest(
                            taskApi.sendEditedTaskMappedChanges(
                                credentials.toAuthBasicDto(),
                                existingInput.id,
                                existingInput.compareWithAndGetDiffs(outToDTO.copy(id = ""))
                            )
                        )
                    }
                } ?: kotlin.run {
                    // if taskDomain is not new
                    result = ErrorRequest<TaskDto>(ThisTaskIsNotNew)
                    // clear id for new taskDomains
                    if (outputTask.newTask) {
                        val withoutId = outToDTO.copy(id = "")
                        // send all params
                        result = taskApi.sendNewTask(credentials.toAuthBasicDto(), withoutId)
                    }
                }
                result?.let { res ->
                    when (res) {
                        is SuccessRequest -> {
                            inputTaskRepository.saveAndDelete(
                                inputTask = res.data.toTaskInputHandledWithUsers(whoAmI.userId),
                                outputTask = outputTask,
                                whoAmI = whoAmI
                            )
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

    override fun inputFetchJobFlow(
        credentials: Credentials,
        whoAmI: UserInput
    ): Flow<Request<List<String>>> =
        flow {
            emit(PendingRequest())
            val result: TaskListDto = taskApi.getTaskList(credentials.toAuthBasicDto())
            var curTime = System.currentTimeMillis()
            if (enabledJobLog) logger.log(TAG, "insertUsers")
            inputTaskRepository.insertUsers(result.toUserInputList())
            if (enabledJobLog) logger.log(
                TAG,
                "insertUsers ${System.currentTimeMillis() - curTime}ms"
            )

            curTime = System.currentTimeMillis()
            if (enabledJobLog) logger.log(TAG, "insertTasks")
            try {
                val taskInputList = result.toTaskInputList(whoAmI.userId)
                if (enabledJobLog) logger.log(TAG, "Count taskInputList: ${taskInputList.size}")
                val pairCounter = inputTaskRepository.insertTasks(taskInputList)
                val deletedTasks = pairCounter.second
                val saveCounter = pairCounter.first
                if (enabledJobLog) logger.log(TAG, "Count tasks to delete: ${deletedTasks}")
                if (enabledJobLog) logger.log(TAG, "Count saved inputTasks: $saveCounter")
            } catch (e: NullPointerException) {
                throw BackendException(errorBody = e.message?.toString() ?: "", errorCode = "")
            }
            if (enabledJobLog) logger.log(
                TAG,
                "insertTasks ${System.currentTimeMillis() - curTime}ms"
            )
            emit(SuccessRequest(result.tasks.map { it.id }))
        }.flowOn(ioDispatcher)

    /**
     * return equal tasks IDs list
     */
    private fun getEqualOutputTasksInInputTasks(
        outputTasks: List<TaskDto>,
        inputTasks: List<TaskDto>
    ): List<String> {
        val listToDelete = arrayListOf<String>()

        outputTasks.forEach { outputTask ->
            if (inputTasks.contains(outputTask)) {
                listToDelete.add(outputTask.id)
            }
        }
        return listToDelete
    }
}