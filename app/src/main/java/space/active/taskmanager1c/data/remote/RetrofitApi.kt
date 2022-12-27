package space.active.taskmanager1c.data.remote

import retrofit2.http.*
import space.active.taskmanager1c.data.remote.dto.TaskListDto
import space.active.taskmanager1c.data.remote.dto.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.remote.dto.messages_dto.TaskMessagesDTOTemp
import space.active.taskmanager1c.data.remote.dto.messages_dto.TaskUserReadingFlagDTO
import space.active.taskmanager1c.data.remote.dto.messages_dto.TasksReadingTimeDTO

interface RetrofitApi {
//    tasks: get - получение списка задач
//    tasks: post - обновление или создание новой задачи
//    tasks\status: post - получение времени чтения и времени последнего сообщения
//    tasks\status\time: post - обновление времени чтения
//    tasks\status\unread: post - установка\снятие флага
//    tasks\messages: get - получение сообщения по task id


    /**
     * GET
     * http://172.16.17.242/torg_develop/hs/taskmgr/tasks
     */
    @GET("tasks")
    suspend fun getTasks(): TaskListDto

    /**
     * POST
     * http://172.16.17.242/torg_develop/hs/taskmgr/tasks
    {
    "id: "dfsdsd",
    "name": "11111Фразы покупателей в магазинах"
    }
     */
    @POST("tasks")
    @Headers("Content-Type: application/json")
    suspend fun saveChanges(@Body changes: String): TaskListDto

    /**
     * GET
     * read messages for task
     * http://172.16.17.242/torg_develop/hs/tasks/messages?id=4ce6cb44-a3c4-11ea-8d5a-00155d28010b
     */
    @GET("tasks/messages")
    suspend fun getMessages(@Query("id") taskId: String): TaskMessagesDTO

    /**
     * POST
     * send messages for task
     * http://172.16.17.242/torg_develop/hs/taskmgr/messages?id=4ce6cb44-a3c4-11ea-8d5a-00155d28010b
    {
    "id": "f54c2dfb-59ab-11ed-a023-da2dec3fdf49",
    "text": "This is a test HTTP-service message for new POST message request!!!"
    }
     */
    @POST("tasks/messages")
    suspend fun sendMessages(@Body map: Map<String, String>): TaskMessagesDTOTemp


    /**
     * POST
     * messages read time
     * http://172.16.17.242/torg_develop/hs/taskmgr/tasks/status
     */
    @POST("tasks/status")
    suspend fun getReadingTimes(@Body taskListIds: List<String>): List<TasksReadingTimeDTO>

    /**
     * POST
     * update task read time
     * http://172.16.17.242/torg_develop/hs/taskmgr/tasks/status
    "id": "f54c2dfb-59ab-11ed-a023-da2dec3fdf49",
    "readTime": "2099-12-31T23:59:59"
     */
    @POST("tasks/status/time")
    suspend fun setReadingTime(@Body map: Map<String, String>): TasksReadingTimeDTO

    @POST("tasks/status/unread")
    suspend fun setReadingFlag(
        @Body taskId: String,
        @Body readingFlag: Boolean
    ): TaskUserReadingFlagDTO

}