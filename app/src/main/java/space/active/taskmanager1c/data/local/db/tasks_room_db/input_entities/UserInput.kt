package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import space.active.taskmanager1c.domain.models.UserDomain

@Entity
data class UserInput(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val name: String
)
{
    fun toUserDomain(): UserDomain = UserDomain (
        id = this.id,
        name = this.name
            )
    companion object {
        fun List<UserInput>.toListUserDomain(): List<UserDomain> {
            return this.map { it.toUserDomain() }
        }
    }
}
