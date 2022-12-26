package space.active.taskmanager1c.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.remote.dto.AuthDto
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto
import space.active.taskmanager1c.data.remote.dto.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.repository.TaskApi
import javax.inject.Inject

private const val TAG = "RetrofitTasksSource"

class RetrofitTasksSource @Inject constructor
    (
    private val logger: Logger,
    private val converters: Converters,
    config: RetrofitConfig
) : BaseRetrofitSource(config), TaskApi {

    private val retrofitApi = retrofit.create(RetrofitApi::class.java)

    override fun getTaskListFlow(): Flow<Request<TaskListDto>> = flow {
        emit(PendingRequest())
        emit(getTaskList())
    }

    override suspend fun getTaskList(): Request<TaskListDto> =
        wrapRetrofitExceptions {
            val taskDto = retrofitApi.getTasks()
            logger.log(TAG, "getTaskList ${taskDto.tasks.joinToString("\n") { it.name }}")
            if (taskDto.tasks.isEmpty() or taskDto.users.isEmpty()) {
                return@wrapRetrofitExceptions ErrorRequest(EmptyObject("TaskListDto"))
            } else {
                return@wrapRetrofitExceptions SuccessRequest(taskDto)
            }
        }

    override suspend fun sendNewTask(task: TaskDto): Request<TaskDto> {
        TODO("Not yet implemented")
    }

    override suspend fun sendEditedTaskMappedChanges(taskId: String, changeMap: Map<String, Any>) = wrapRetrofitExceptions<TaskDto> {
        val mapWId = mutableMapOf<String, Any>()
        mapWId["id"] = taskId
        mapWId.putAll(changeMap)
        val changes = converters.mapToJson(mapWId)
        logger.log(TAG, "Send to server: $changes")
        val res = retrofitApi.saveChanges(changes)
        return@wrapRetrofitExceptions res.tasks.first()
    }

    override suspend fun getMessages(taskId: String): TaskMessagesDTO = wrapRetrofitExceptions {
        retrofitApi.getMessages(taskId)
    }

    override suspend fun getMessagesTimes(taskIds: List<String>): List<TaskMessagesDTO> = wrapRetrofitExceptions{
        TODO("Not yet implemented")
    }

    override suspend fun authUser(username: String, password: String): Request<AuthDto> = wrapRetrofitExceptions {
        TODO("Not yet implemented")
    }
}