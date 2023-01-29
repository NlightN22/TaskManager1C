package space.active.taskmanager1c.data.remote.retrofit

import retrofit2.http.*
import space.active.taskmanager1c.data.remote.model.TaskDto
import space.active.taskmanager1c.data.remote.model.TaskListDto
import space.active.taskmanager1c.data.remote.model.UserDto
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskUserReadingFlagDTO
import space.active.taskmanager1c.data.remote.model.reading_times.FetchReadingTimes
import space.active.taskmanager1c.data.remote.model.reading_times.ReadingTimesDTO
import space.active.taskmanager1c.data.remote.model.reading_times.ReadingTimesTaskDTO
import space.active.taskmanager1c.data.remote.model.reading_times.SetReadingTimeDTO

interface RetrofitApi {
    /**
     * GET
     * https://host.name/torg_develop/hs/taskmgr/tasks
     */
    @GET("tasks")
    suspend fun getTasks(
        @Header("Authorization") auth: String,
    ): TaskListDto

    /**
     * POST
     * save new task
     * https://host.name/torg_develop/hs/taskmgr/tasks
     */
    @POST("tasks")
    suspend fun sendNew(
        @Header("Authorization") auth: String,
        @Body taskDto: TaskDto
    ): TaskListDto

    /**
     * POST
     * save tasks changes
     * https://host.name/torg_develop/hs/taskmgr/tasks/taskId
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
     * https://host.name/torg_develop/hs/tasks/messages?id=4ce6cb44-a3c4-11ea-8d5a-00155d28010b
     */
    @GET("tasks/messages")
    suspend fun getMessages(
        @Header("Authorization") auth: String,
        @Query("id") taskId: String
    ): TaskMessagesDTO

    /**
     * POST
     * send messages for task
     * https://host.name/torg_develop/hs/taskmgr/messages?id=4ce6cb44-a3c4-11ea-8d5a-00155d28010b
     *   {
     *   "id": "f54c2dfb-59ab-11ed-a023-da2dec3fdf49",
     *   "text": "This is a test HTTP-service message for new POST message request!!!"
     *   }
     */
    @POST("tasks/messages")
    suspend fun sendMessages(
        @Header("Authorization") auth: String,
        @Body map: Map<String, String>
    ): TaskMessagesDTO

    /**
     * POST
     * messages read time
     * https://host.name/torg_develop/hs/taskmgr/tasks/status
     */
    @POST("tasks/status")
    suspend fun getReadingTimes(
        @Header("Authorization") auth: String,
        @Body taskListIds: FetchReadingTimes
    ): ReadingTimesDTO

    /**
     * POST
     * update task read time
     * https://host.name/torg_develop/hs/taskmgr/tasks/status/time
    "id": "f54c2dfb-59ab-11ed-a023-da2dec3fdf49",
    "readTime": "2099-12-31T23:59:59",
    "messageTime" : "2099-12-31T23:59:59"
     */
    @POST("tasks/status/time")
    suspend fun setReadingTime(
        @Header("Authorization") auth: String,
        @Body setReadingTimeDTO: SetReadingTimeDTO
    ): ReadingTimesTaskDTO

    /**
     * POST
     * enable/disable unread flag for user
     * "id": "f54c2dfb-59ab-11ed-a023-da2dec3fdf49",
     * "flag": true
     * https://host.name/torg_develop/hs/taskmgr/tasks/status/unread
     */
    @POST("tasks/status/unread")
    suspend fun setReadingFlag(
        @Header("Authorization") auth: String,
        @Body map: Map<String, String>
    ): TaskUserReadingFlagDTO

    /**
     * GET
     * user authorization
     * https://host.name/torg_develop/hs/taskmgr/auth
     */
    @GET("auth")
    suspend fun authUser(@Header("Authorization") auth: String): UserDto
}