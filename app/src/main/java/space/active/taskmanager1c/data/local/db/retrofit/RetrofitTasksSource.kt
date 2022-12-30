package space.active.taskmanager1c.data.local.db.retrofit

import okhttp3.Credentials
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.remote.TaskApi
import space.active.taskmanager1c.data.remote.model.AuthBasicDto
import space.active.taskmanager1c.data.remote.model.TaskDto
import space.active.taskmanager1c.data.remote.model.TaskListDto
import space.active.taskmanager1c.data.remote.model.UserDto
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskUserReadingFlagDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TasksReadingTimeDTO
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import javax.inject.Inject

private const val TAG = "RetrofitTasksSource"

class RetrofitTasksSource @Inject constructor
    (
    private val logger: Logger,
    private val converters: Converters,
    config: RetrofitConfig
) : BaseRetrofitSource(config), TaskApi {

    private val retrofitApi = retrofit.create(RetrofitApi::class.java)

    override suspend fun authUser(auth: AuthBasicDto): UserDto = wrapRetrofitExceptions {
        retrofitApi.authUser(auth.toBasic())
    }

    override suspend fun getTaskList(auth: AuthBasicDto): Request<TaskListDto> =
        wrapRetrofitExceptions {
            val taskDto = retrofitApi.getTasks(auth.toBasic())
//            logger.log(TAG, "getTaskList ${taskDto.tasks.joinToString("\n") { it.name }}")
            if (taskDto.tasks.isEmpty() or taskDto.users.isEmpty()) {
                return@wrapRetrofitExceptions ErrorRequest(EmptyObject("TaskListDto"))
            } else {
                return@wrapRetrofitExceptions SuccessRequest(taskDto)
            }
        }

    override suspend fun sendNewTask(auth: AuthBasicDto, task: TaskDto): Request<TaskDto> =
        wrapRetrofitExceptions {
            logger.log(TAG, "sendNewTask: $task")
            val res = retrofitApi.sendNew(auth.toBasic(), task).tasks.first()
            logger.log(TAG, "Get from server task: $res")
            SuccessRequest(res)
        }

    override suspend fun sendEditedTaskMappedChanges(
        auth: AuthBasicDto,
        taskId: String,
        changeMap: Map<String, Any>
    ) = wrapRetrofitExceptions<TaskDto> {
        val mapWId = mutableMapOf<String, Any>()
        mapWId["id"] = taskId
        mapWId.putAll(changeMap)
        val changes = converters.mapToJson(mapWId)
        logger.log(TAG, "Send changes: $changes")
        val res = retrofitApi.saveChanges(auth.toBasic(), changes)
        res.tasks.first()
    }

    override suspend fun getMessages(auth: AuthBasicDto, taskId: String): TaskMessagesDTO =
        wrapRetrofitExceptions {
            retrofitApi.getMessages(auth.toBasic(), taskId)
        }

    override suspend fun sendMessage(
        auth: AuthBasicDto,
        taskId: String,
        text: String
    ): TaskMessagesDTO = wrapRetrofitExceptions {
        retrofitApi.sendMessages(auth.toBasic(), mapOf("id" to taskId, "text" to text))
    }

    override suspend fun getMessagesTimes(
        auth: AuthBasicDto,
        taskIds: List<String>
    ): List<TasksReadingTimeDTO> = wrapRetrofitExceptions {
        retrofitApi.getReadingTimes(auth.toBasic(), taskIds)
    }

    override suspend fun setReadingTime(
        auth: AuthBasicDto,
        taskId: String,
        readingTime: LocalDateTime
    ): TasksReadingTimeDTO = wrapRetrofitExceptions {
        val toSendMap = mapOf<String,String>("id" to taskId, "readTime" to readingTime.toString())
        retrofitApi.setReadingTime(auth.toBasic(),toSendMap)
    }

    override suspend fun setReadingFlag(auth: AuthBasicDto, taskId: String, flag: Boolean): TaskUserReadingFlagDTO = wrapRetrofitExceptions {
        val toSendMap = mapOf<String,String>("id" to taskId, "flag" to flag.toString())
        retrofitApi.setReadingFlag(auth.toBasic(), toSendMap)
    }

    private fun AuthBasicDto.toBasic(): String =
        Credentials.basic(this.name, this.pass, StandardCharsets.UTF_8)


}