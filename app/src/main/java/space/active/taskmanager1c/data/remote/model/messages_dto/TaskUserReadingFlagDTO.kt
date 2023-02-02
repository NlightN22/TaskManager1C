package space.active.taskmanager1c.data.remote.model.messages_dto

data class TaskUserReadingFlagDTO(
    val flag: String,
    val id: String,
    val user: String,
    val version: Int,
)
{
    fun flagToBoolean() : Boolean {
        return flag == "true"
    }
}