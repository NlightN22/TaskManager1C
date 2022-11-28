package space.active.taskmanager1c.data.remote.dto

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.MessageInput

data class MessageDto(
    val authorId: String,
    val date: String,
    val id: String,
    val text: String
) {
    fun toMessageDb(taskId: String): MessageInput {
        return MessageInput(
            authorId = authorId,
            date = date,
            id = id,
            text = text,
            taskId = taskId
        )
    }
}