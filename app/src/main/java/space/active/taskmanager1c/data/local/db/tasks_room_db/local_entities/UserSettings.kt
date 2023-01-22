package space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.coreutils.EncryptedData
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput

@Entity
data class UserSettings (
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val userId: String? = null,
    val username: EncryptedData? = null,
    val password: EncryptedData? = null,
    val serverAddress: EncryptedData? = null,
) {


    fun toUserInput() = UserInput(
        userId = this.userId ?: throw EmptyObject("username"),
        userName = this.username?.getString() ?: throw EmptyObject("password")
    )
}