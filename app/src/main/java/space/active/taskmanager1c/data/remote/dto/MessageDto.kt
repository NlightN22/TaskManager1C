package space.active.taskmanager1c.data.remote.dto

import space.active.taskmanager1c.data.local.db.TasksFromRemote.entity.MessageDb

data class MessageDto(
    val authorId: String,
    val date: String,
    val id: String,
    val text: String
) {
    fun toMessageDb(taskId: String): MessageDb {
        return MessageDb(
            authorId = authorId,
            date = date,
            id = id,
            text = text,
            taskId = taskId
        )
    }
}