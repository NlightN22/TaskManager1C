package space.active.taskmanager1c.data.remote

import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto
import space.active.taskmanager1c.data.remote.dto.UserDto

interface TaskApi {
    suspend fun getTaskList(): List<TaskListDto>
    suspend fun getAllUsers(): List<UserDto>
    suspend fun sendTaskChanges(task: TaskDto)
    suspend fun sendCredentials(username: String, password: String)
}