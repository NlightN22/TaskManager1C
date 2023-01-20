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
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.TaskInput
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

    override fun updateJob(
        credentials: Credentials,
        updateDelay: Long,
        whoAmI: UserInput
    ): Flow<Request<Any>> =
        channelFlow<Request<Any>> {
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
            updateReadingState(credentials).collect {
                send(it)
            }
            logger.log(TAG, "updateReadingState stop ${System.currentTimeMillis() - curTime}ms")

            delay(updateDelay)
        }.flowOn(ioDispatcher)

    // todo change to update after show list at screen
    private fun updateReadingState(credentials: Credentials) = flow<Request<Any>> {
        emit(PendingRequest())
        val result: List<ReadingTimesTask> = taskApi.getMessagesTimes(
            credentials.toAuthBasicDto(),
            inputTaskRepository.getTasks().map { it.taskIn.id })
        result.forEach {
            inputTaskRepository.updateReading(it.id, it.getUnreadStatus())
        }
        emit(SuccessRequest(Any()))
    }

    private fun outputSendJob(credentials: Credentials, whoAmI: UserInput) = flow<Request<Any>> {
        val outputTasks: List<OutputTask> = outputTaskRepository.getTasks()
        val inputTasks: List<TaskInput> = inputTaskRepository.getTasks().map { it.taskIn }
        if (outputTasks.isNotEmpty()) {
            // find an delete the same taskDomains in output and input table
            val deleteOutputTaskList: List<OutputTask> =
                getEqualOutputTasksInInputTasks(outputTasks, inputTasks)
            outputTaskRepository.deleteTasks(deleteOutputTaskList)
            // get only not deleted output taskDomains
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
                    // if taskDomain is not edited and existing in input table
                    if (!outputTask.newTask) {

                        val inputDTO = TaskDto.fromInputTask(it.taskIn)
                        result = SuccessRequest(
                            taskApi.sendEditedTaskMappedChanges(
                                credentials.toAuthBasicDto(),
                                inputDTO.id,
                                inputDTO.compareWithAndGetDiffs(outToDTO.copy(id = ""))
                            )
                        )
//                         todo delete mock
//                        result = SuccessRequest(inputDTO)
                    }
                } ?: kotlin.run {
                    // if taskDomain is not new
                    result = ErrorRequest<TaskDto>(ThisTaskIsNotNew)
                    // clear id for new taskDomains
                    if (outputTask.newTask) {
                        val withoutId = outToDTO.copy(id = "")
                        // send all params
                        result = taskApi.sendNewTask(credentials.toAuthBasicDto(),withoutId)
                        // todo delete mock
//                        delay(6000)
//                        result = SuccessRequest(withoutId)
                    }
                }
                result?.let { res ->
                    when (res) {
                        is SuccessRequest -> {
                            // TODO if taskDomain send with delete label
//                        logger.log(TAG, result.data.toString())
                            inputTaskRepository.saveAndDelete(
                                inputTask =res.data.toTaskInput(),
                                outputTask = outputTask,
                                whoAmI = whoAmI
                            )
//                            inputTaskRepository.insertTask(
//                                ,
//                                whoAmI
//                            )
//                            delay(100)
//                            outputTaskRepository.deleteTasks(listOf(outputTask))
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

    override fun inputFetchJobFlow(credentials: Credentials, whoAmI: UserInput) =
        flow<Request<Any>> {
            emit(PendingRequest())
            val result: TaskListDto = taskApi.getTaskList(credentials.toAuthBasicDto())
            var curTime = System.currentTimeMillis()
            logger.log(TAG, "insertUsers")
            inputTaskRepository.insertUsers(result.toUserInputList())
            logger.log(TAG, "insertUsers ${System.currentTimeMillis() - curTime}ms")

            curTime = System.currentTimeMillis()
            logger.log(TAG, "insertTasks")
            inputTaskRepository.insertTasks(result.toTaskInputList(), whoAmI)
            logger.log(TAG, "insertTasks ${System.currentTimeMillis() - curTime}ms")
            emit(SuccessRequest(Any()))
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