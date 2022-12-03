package space.active.taskmanager1c.data.remote

import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.lastOrNull
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto
import space.active.taskmanager1c.data.remote.test.GetFromFile
import space.active.taskmanager1c.data.repository.TaskApi
import space.active.taskmanager1c.data.utils.JsonParser
import javax.inject.Inject

private const val TAG = "TaskApiImpl"

class TaskApiImpl @Inject constructor(
    private val jsonParser: JsonParser,
    private val getFromFile: GetFromFile,
    private val logger: Logger
): TaskApi {

    override fun getTaskListFlow(): Flow<Request<TaskListDto>> = flow {
        emit(PendingRequest())
        try {
            emit(PendingRequest())
//            val result = TaskListDto(tasks = emptyList(), users = emptyList())
            val result = jsonParser
                .fromJson<TaskListDto>(
                    getFromFile.invoke(),
                    object : TypeToken<TaskListDto>(){}.type
                ) ?: throw NullAnswerFromServer()
            emit(SuccessRequest(result))
        } catch ( e: Exception) {
            emit(ErrorRequest(e))
        }
    }

    override suspend fun sendTaskChanges(task: TaskDto): Request<TaskDto> {
        // TODO mock replace to real impl
        return try {
            logger.log(TAG, task.toString())
            SuccessRequest(task)
        } catch (e: Exception) {
            ErrorRequest(e)
        }

    }

    override suspend fun authUser(username: String, password: String) {
        TODO("Not yet implemented")
    }


    // TODO Change to Flow
    override suspend fun getTaskList(): Request<TaskListDto> {
        return try {
            val result = getTaskListFlow().lastOrNull()
            result ?: ErrorRequest(EmptyObject)
        } catch (e: Exception) {
            ErrorRequest(e)
        }
    }
}