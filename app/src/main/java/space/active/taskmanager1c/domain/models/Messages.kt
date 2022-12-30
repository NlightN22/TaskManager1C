package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.coreutils.toDateTime
import space.active.taskmanager1c.coreutils.toDateTimeOrNull
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessageDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesUserDTO
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Messages(
    val userName: String,
    val authorId: String,
    val dateTime: LocalDateTime,
    val id: String,
    val text: String,
    val readingTime: LocalDateTime?,
    var my: Boolean = false
) {
    companion object {
        fun TaskMessageDTO.toMessage(userName: String, readingTime: String) = Messages(
            userName = userName,
            authorId = this.authorId,
            dateTime = this.date.toDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            id = this.id,
            text = this.text,
            readingTime = readingTime.toDateTimeOrNull(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )

        fun List<TaskMessageDTO>.toMessages(listUsers: List<TaskMessagesUserDTO>, readingTime: String): List<Messages> =
            this.map { messageDTO ->
                val usersIds = listUsers.filter {it.id == messageDTO.authorId}
                if (usersIds.isEmpty()) {
                    messageDTO.toMessage("", readingTime)
                } else {
                    messageDTO.toMessage(usersIds.first().name, readingTime)
                }
            }
    }
}
