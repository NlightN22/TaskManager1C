package space.active.taskmanager1c.data.remote

import android.content.Context
import android.util.Log
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto
import space.active.taskmanager1c.data.remote.dto.UserDto
import space.active.taskmanager1c.data.utils.JsonParser

private const val TAG = "DemoData"

class DemoData: TaskApi {
    override suspend fun getTaskList(): List<TaskListDto> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllUsers(): List<UserDto> {
        TODO("Not yet implemented")
    }

    override suspend fun sendTaskChanges(task: TaskDto) {
        TODO("Not yet implemented")
    }

    override suspend fun sendCredentials(username: String, password: String) {
        TODO("Not yet implemented")
    }

    fun getDemoData(context: Context, jsonParser: JsonParser): Any? {
        val files = context.filesDir.listFiles()
        files.forEach { Log.e(TAG, "getDemoData files: ${it.name}") }
        return files
            .filter { it.canRead() && it.isFile && it.name.startsWith("api_example") }
            .map {
                it.readText()
//                jsonParser.fromJson(
//                    it.readText(),
//                    object : TypeToken<List<TaskListDto>>() {}.type
//                )
            }
    }
}

