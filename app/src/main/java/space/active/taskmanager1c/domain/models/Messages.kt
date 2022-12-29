package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.coreutils.toDateTime
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
    var my: Boolean = false
) {
    companion object {
        fun TaskMessageDTO.toMessage(userName: String) = Messages(
            userName = userName,
            authorId = this.authorId,
            dateTime = this.date.toDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            id = this.id,
            text = this.text
        )

        fun List<TaskMessageDTO>.toMessages(listUsers: List<TaskMessagesUserDTO>): List<Messages> =
            this.map { messageDTO ->
                val usersIds = listUsers.filter {it.id == messageDTO.authorId}
                if (usersIds.isEmpty()) {
                    messageDTO.toMessage("")
                } else {
                    messageDTO.toMessage(usersIds.first().name)
                }
            }
    }
}
