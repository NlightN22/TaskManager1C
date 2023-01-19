package space.active.taskmanager1c.data.remote.model.reading_times

import space.active.taskmanager1c.coreutils.toDateTime
import java.time.format.DateTimeFormatter

data class ReadingTimesTask(
    val id: String,
    val lastMessageTime: String,
    val readingTime: String
){
    fun getUnreadStatus(): Boolean {
        val messageTime = lastMessageTime.toDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val lastRead = readingTime.toDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return (messageTime > lastRead)
    }
}