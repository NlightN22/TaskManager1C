package space.active.taskmanager1c.data.repository

import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.domain.models.Task

interface OutputTaskRepository {
    fun saveChangesAndSand(task: Task): Request<Boolean>
    fun getOutputTasks(): Request<List<Task>>
}