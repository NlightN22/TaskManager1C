package space.active.taskmanager1c.data.remote.model.reading_times

import space.active.taskmanager1c.coreutils.toZonedDateTime
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.ReadingTimesTaskEntity
import java.time.format.DateTimeFormatter

data class ReadingTimesTaskDTO(
    val id: String,
    val lastMessageTime: String,
    val readingTime: String
) {
    fun toReadingTimesTaskEntity(): ReadingTimesTaskEntity = ReadingTimesTaskEntity(
        mainTaskId = id,
        lastMessageTime = lastMessageTime,
        taskReadingTime = readingTime,
        isUnread = getUnreadStatus()
    )

    fun getUnreadStatus(): Boolean {
        if (lastMessageTime.isNotBlank()) {
            val messageTime = lastMessageTime.toZonedDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val lastRead = readingTime.toZonedDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
//            if (messageTime > lastRead) {
//                Log.d("incomeReadingTimes", "$id  $lastMessageTime > $readingTime")
//                Log.d("getUnreadStatus", "$id  $messageTime > $lastRead")
//            }
            return (messageTime > lastRead)
        }
        return false
    }
}