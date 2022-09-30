package space.active.taskmanager1c.data.remote

import space.active.taskmanager1c.data.remote.dto.ExampleData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto
import space.active.taskmanager1c.data.utils.GsonParserImpl
import space.active.taskmanager1c.data.utils.JsonParser

private const val TAG = "DemoData"

class DemoData(
    private val exampleData: ExampleData = ExampleData(),
    private val jsonParser: JsonParser = GsonParserImpl(Gson())
) : TaskApi {
    override suspend fun getTaskList(): TaskListDto {
        return jsonParser
            .fromJson<TaskListDto>(
                exampleData.data,
                object : TypeToken <TaskListDto>(){}.type
            ) ?: TaskListDto()
    }

    override suspend fun sendTaskChanges(task: TaskDto) {
        TODO("Not yet implemented")
    }

    override suspend fun authUser(username: String, password: String) {
        TODO("Not yet implemented")
    }
}

