package space.active.taskmanager1c.data.remote.retrofit

import retrofit2.http.*
import space.active.taskmanager1c.data.remote.model.TaskDto
import space.active.taskmanager1c.data.remote.model.TaskListDto
import space.active.taskmanager1c.data.remote.model.UserDto
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskUserReadingFlagDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TasksReadingTimeDTO
import space.active.taskmanager1c.data.remote.model.reading_times.FetchReadingTimes
import space.active.taskmanager1c.data.remote.model.reading_times.ReadingTimesDTO

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
    suspend fun getTasks(
        @Header("Authorization") auth: String,
    ): TaskListDto

    /**
     * POST
     * save new task
     * http://172.16.17.242/torg_develop/hs/taskmgr/tasks
     */
    @POST("tasks")
    suspend fun sendNew(
        @Header("Authorization") auth: String,
        @Body taskDto: TaskDto
    ): TaskListDto

    /**
     * POST
     * save tasks changes
     * http://172.16.17.242/torg_develop/hs/taskmgr/tasks/taskId
    {
    "id: "dfsdsd",
    "name": "11111Фразы покупателей в магазинах"
    }
     */
    @POST("tasks/{taskId}")
    @Headers("Content-Type: application/json")
    suspend fun saveChanges(
        @Path("taskId") taskId: String,
        @Header("Authorization") auth: String,
        @Body changes: String
    ): TaskListDto

    /**
     * GET
     * read messages for task
     * http://172.16.17.242/torg_develop/hs/tasks/messages?id=4ce6cb44-a3c4-11ea-8d5a-00155d28010b
     */
    @GET("tasks/messages")
    suspend fun getMessages(
        @Header("Authorization") auth: String,
        @Query("id") taskId: String
    ): TaskMessagesDTO

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
    suspend fun sendMessages(
        @Header("Authorization") auth: String,
        @Body map: Map<String, String>
    ): TaskMessagesDTO

    /**
     * POST
     * messages read time
     * http://172.16.17.242/torg_develop/hs/taskmgr/tasks/status
     */
    @POST("tasks/status")
    suspend fun getReadingTimes(
        @Header("Authorization") auth: String,
        @Body taskListIds: FetchReadingTimes
    ): ReadingTimesDTO

    /**
     * POST
     * update task read time
     * http://172.16.17.242/torg_develop/hs/taskmgr/tasks/status
    "id": "f54c2dfb-59ab-11ed-a023-da2dec3fdf49",
    "readTime": "2099-12-31T23:59:59"
     */
    @POST("tasks/status/time")
    suspend fun setReadingTime(
        @Header("Authorization") auth: String,
        @Body map: Map<String, String>
    ): TasksReadingTimeDTO

    /**
     * POST
     * enable/disable unread flag for user
    "id": "f54c2dfb-59ab-11ed-a023-da2dec3fdf49",
    "flag": true
     */
    @POST("tasks/status/unread")
    suspend fun setReadingFlag(
        @Header("Authorization") auth: String,
        @Body map: Map<String, String>
    ): TaskUserReadingFlagDTO

    /**
     * GET
     * user authorization
     */
    @GET("auth")
    suspend fun authUser(@Header("Authorization") auth: String): UserDto

}