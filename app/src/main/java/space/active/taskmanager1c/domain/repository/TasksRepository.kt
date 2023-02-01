package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.domain.models.TaskDomain
import space.active.taskmanager1c.domain.models.TaskListFilterTypes
import space.active.taskmanager1c.domain.models.TaskListOrderTypes
import space.active.taskmanager1c.domain.models.UserDomain

interface TasksRepository {
    suspend fun getInputTasksCount(): Int
    fun getTasksFiltered(filterTypes: Flow<TaskListFilterTypes>,
                         orderTypes: Flow<TaskListOrderTypes>,
                         myId: Flow<String>
    ): Flow<List<TaskDomain>>
    val listUsersFlow: Flow<List<UserDomain>>
    fun getTask(taskId: String): Flow<TaskDomain?>
    fun editTask(taskDomain: TaskDomain, myId:String): Flow<Request<Any>>
    fun createNewTask(taskDomain: TaskDomain, myId:String): Flow<Request<Any>>
}