package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.coreutils.toZonedDateTime
import space.active.taskmanager1c.coreutils.toZonedDateTimeOrNull
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessageDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesUserDTO
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class Messages(
    val userName: String,
    val authorId: String,
    val dateTime: ZonedDateTime,
    val id: String,
    val text: String,
    val unread: Boolean = false,
    var my: Boolean = false
) {
    companion object {
        fun TaskMessageDTO.toMessage(userName: String, readingTime: String): Messages {
            val messageTime = this.date.toZonedDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val taskTime = readingTime.toZonedDateTimeOrNull(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            return Messages(
                userName = userName,
                authorId = this.authorId,
                dateTime = messageTime,
                id = this.id,
                text = this.text,
                unread = setUnreadState(taskTime , messageTime)
            )
        }

        private fun setUnreadState(taskTime: ZonedDateTime?, messageTime: ZonedDateTime): Boolean {
            return taskTime?.let { taskReadingTime ->
                messageTime > taskReadingTime
            } ?: false
        }

        private fun setUnreadState(taskTime: LocalDateTime?, messageTime: LocalDateTime): Boolean {
            return taskTime?.let { taskReadingTime ->
                messageTime > taskReadingTime
            } ?: false
        }

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
