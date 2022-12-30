package space.active.taskmanager1c.data.remote.model.messages_dto

import space.active.taskmanager1c.coreutils.toDateTime
import java.time.format.DateTimeFormatter

data class TasksReadingTimeDTO(
    val id: String,
    val lastMessageReadTime: String,
    val readingTime: String
) {
    fun gerUnreadStatus(): Boolean {
        val messageTime = lastMessageReadTime.toDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val lastRead = readingTime.toDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return (messageTime > lastRead)
    }

}