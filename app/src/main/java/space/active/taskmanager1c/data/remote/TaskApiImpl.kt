package space.active.taskmanager1c.data.remote

import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto
import space.active.taskmanager1c.data.remote.test.GetFromFile
import space.active.taskmanager1c.data.repository.TaskApi
import space.active.taskmanager1c.data.utils.JsonParser

class TaskApiImpl(
    private val jsonParser: JsonParser,
    private val getFromFile: GetFromFile
): TaskApi {
    override fun getTaskListFlow(): Flow<Request<TaskListDto>> = flow {
        emit(PendingRequest())
        try {
            emit(PendingRequest())
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
        TODO("Not yet implemented")
    }

    override suspend fun authUser(username: String, password: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getTaskList(): Request<TaskListDto> {
        TODO("Not yet implemented")
    }
}