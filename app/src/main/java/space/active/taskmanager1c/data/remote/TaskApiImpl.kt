package space.active.taskmanager1c.data.remote

import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.lastOrNull
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.remote.dto.AuthDto
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto
import space.active.taskmanager1c.data.remote.test.TaskApiMockk
import space.active.taskmanager1c.data.repository.TaskApi
import space.active.taskmanager1c.data.utils.JsonParser
import javax.inject.Inject

private const val TAG = "TaskApiImpl"

class TaskApiImpl @Inject constructor(
    private val taskApiMockk: TaskApiMockk,
    private val logger: Logger,
    private val converters: Converters
): TaskApi {

    override fun getTaskListFlow(): Flow<Request<TaskListDto>> = flow {
        // TODO replace to real impl
        emit(PendingRequest())
        try {
            emit(PendingRequest())
//            val result = TaskListDto(tasks = emptyList(), users = emptyList())

            val strFromMock = taskApiMockk.getTaskDtoFromFile()

            val result = converters.taskListDtoFromJson(strFromMock) ?: throw NullAnswerFromServer()
            emit(SuccessRequest(result))
        } catch ( e: Exception) {
            emit(ErrorRequest(e))
        }
    }

    override suspend fun sendNewTask(task: TaskDto): Request<TaskDto> {
        // TODO replace to real impl
        return try {
            logger.log(TAG, task.toString())
            SuccessRequest(task)
        } catch (e: Exception) {
            ErrorRequest(e)
        }

    }

    override suspend fun sendEditedTaskMappedChanges(changeMap: Map<String, Any?>): Request<TaskDto> {
        // TODO replace to real impl
        val startString = "\"task\":"
        val json = converters.mapToJson(changeMap)
        val final = startString + json
        logger.log(TAG, "Send to server: $final")
        delay(1000)
        val answer: TaskDto =  taskApiMockk.getSavedChangesTask()
        logger.log(TAG, "Get from server: $answer")
        return SuccessRequest(answer)

    }

    override suspend fun authUser(username: String, password: String): Request<AuthDto> {
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