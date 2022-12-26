package space.active.taskmanager1c.data.remote

import android.text.TextPaint
import com.google.gson.JsonElement
import retrofit2.http.*
import space.active.taskmanager1c.data.remote.dto.MessageDto
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto
import space.active.taskmanager1c.data.remote.dto.messages_dto.TaskMessagesDTO

interface RetrofitApi {
    /**
     * GET
     * http://172.16.17.242/torg_develop/hs/taskmgr/tasks
     */
    @GET("tasks")
    suspend fun getTasks(): TaskListDto

    /**
     * GET
     * read messages for task
     * http://172.16.17.242/torg_develop/hs/taskmgr/messages?id=4ce6cb44-a3c4-11ea-8d5a-00155d28010b
     */
    @GET("messages")
    suspend fun getMessages(@Query("id") taskId: String): TaskMessagesDTO


    /**
     * POST
     * http://172.16.17.242/torg_develop/hs/taskmgr/tasks
     {
    "id: "dfsdsd",
    "name": "11111Фразы покупателей в магазинах"
    }
     */
    @PUT("tasks")
    @Headers("Content-Type: application/json")
    suspend fun saveChanges(@Body changes: String): TaskListDto

    /**
     * POST
     * messages read time
     * http://172.16.17.242/torg_develop/hs/taskmgr/tasks/list
     */
    @POST("tasks/list")
    suspend fun getMessagesTimes(@Body taskListIds: List<String>): List<TaskMessagesDTO>

}