package space.active.taskmanager1c.data.remote

import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto

interface TaskApi {
    suspend fun getTaskList(): TaskListDto
    suspend fun sendTaskChanges(task: TaskDto)
    suspend fun authUser(username: String, password: String)
}