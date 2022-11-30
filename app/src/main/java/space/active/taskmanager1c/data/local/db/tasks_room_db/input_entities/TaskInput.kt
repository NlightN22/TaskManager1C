package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.UsersInTask

@Entity
data class TaskInput(
    val date: String,
    val description: String,
    val endDate: String,
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val mainTaskId: String,
    val name: String,
    val number: String,
    val objName: String,
    val photos: List<String>,
    val priority: String,
    val status: String,
    @Embedded val usersInTask: UsersInTask
) {
    companion object {
        /**
         * In params is list where we take items with unique ID and replace by them current list
         * Return List with replaced items
         */
        fun List<TaskInput>.mapAndReplaceById(inputList: List<TaskInput>): List<TaskInput> {
            val replacedList = this.map { list1Item ->
                inputList.find { list2Item -> (list1Item.id == list2Item.id) } ?: list1Item
            }
            return replacedList
        }
    }
}
