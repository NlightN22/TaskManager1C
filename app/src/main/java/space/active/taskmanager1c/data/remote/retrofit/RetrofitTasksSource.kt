package space.active.taskmanager1c.data.remote.retrofit

import okhttp3.Credentials
import retrofit2.Retrofit
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.remote.TaskApi
import space.active.taskmanager1c.data.remote.model.AuthBasicDto
import space.active.taskmanager1c.data.remote.model.TaskDto
import space.active.taskmanager1c.data.remote.model.TaskListDto
import space.active.taskmanager1c.data.remote.model.UserDto
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskUserReadingFlagDTO
import space.active.taskmanager1c.data.remote.model.reading_times.FetchReadingTimes
import space.active.taskmanager1c.data.remote.model.reading_times.ReadingTimesTaskDTO
import space.active.taskmanager1c.data.remote.model.reading_times.SetReadingTimeDTO
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import javax.inject.Inject

private const val TAG = "RetrofitTasksSource"

class RetrofitTasksSource @Inject constructor
    (
    private val logger: Logger,
    private val converters: Converters,
    private val retrofit: Retrofit
) : BaseRetrofitSource(), TaskApi {

    private val retrofitApi = retrofit.create(RetrofitApi::class.java)

    override suspend fun authUser(auth: AuthBasicDto): UserDto {
        return wrapRetrofitExceptions {
            retrofitApi.authUser(auth.toBasic())
        }
    }

    override suspend fun getTaskList(auth: AuthBasicDto): TaskListDto =
        wrapRetrofitExceptions {
            retrofitApi.getTasks(auth.toBasic())
        }

    override suspend fun sendNewTask(auth: AuthBasicDto, task: TaskDto): Request<TaskDto> =
        wrapRetrofitExceptions(task) {
            logger.log(TAG, "sendNewTask: $task")
            val res = retrofitApi.sendNew(auth.toBasic(), task).tasks.first()
            logger.log(TAG, "Get from server taskDomain: $res")
            SuccessRequest(res)
        }

    override suspend fun sendEditedTaskMappedChanges(
        auth: AuthBasicDto,
        taskId: String,
        changeMap: Map<String, Any>
    ) = wrapRetrofitExceptions(Pair(taskId, changeMap)) {
        val changes = converters.mapToJson(changeMap)
        logger.log(TAG, "Send changes: $changeMap")
        val res = retrofitApi.saveChanges(taskId, auth.toBasic(), changes)
        res.tasks.first()
    }

    override suspend fun getMessages(auth: AuthBasicDto, taskId: String): TaskMessagesDTO =
        wrapRetrofitExceptions(taskId) {
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
    ): List<ReadingTimesTaskDTO> = wrapRetrofitExceptions(taskIds) {
        val toServer = FetchReadingTimes(taskIds)
//        logger.log(TAG, "Send to server: $toServer")
        retrofitApi.getReadingTimes(auth.toBasic(), toServer).Tasks
    }

    override suspend fun setReadingTime(
        auth: AuthBasicDto,
        taskId: String,
        messageTime: LocalDateTime,
        readingTime: LocalDateTime
    ): ReadingTimesTaskDTO = wrapRetrofitExceptions(Triple(taskId, messageTime,readingTime)) {
        retrofitApi.setReadingTime(auth.toBasic(),
            SetReadingTimeDTO(
                taskId,
                messageTime.toString(),
                readingTime.toString()
            )
            )
    }

    override suspend fun setReadingFlag(
        auth: AuthBasicDto,
        taskId: String,
        flag: Boolean
    ): TaskUserReadingFlagDTO = wrapRetrofitExceptions(Pair(taskId, flag)) {
        val toSendMap = mapOf<String, String>("id" to taskId, "flag" to flag.toString())
        retrofitApi.setReadingFlag(auth.toBasic(), toSendMap)
    }

    private fun AuthBasicDto.toBasic(): String =
        Credentials.basic(this.name, this.pass, StandardCharsets.UTF_8)
}