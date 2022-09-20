package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.data.remote.dto.MessageDto

data class Task(
    val authorId: String,
    val coPerformers: List<String>,
    val date: String,
    val description: String,
    val endDate: String,
    val id: String,
    val mainTaskName: String,
    val mainTaskId: String,
    val messageDto: List<Messages>,
    val name: String,
    val number: String,
    val objName: String,
    val observers: List<String>,
    val performer: String,
    val performerId: String,
    val photos: List<String>,
    val priority: String,
    val status: String
)
