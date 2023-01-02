package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.Label
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.TaskExtra
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.TaskExtraLabelCrossRef
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.TaskInAndExtra
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask


@Database(
    entities = [
        TaskInput::class,
        UserInput::class,
        OutputTask::class,
        TaskExtra::class,
        Label::class,
        TaskExtraLabelCrossRef::class
    ],
    version = 1,

    )
@TypeConverters(Converters::class)
abstract class TaskWithUsersDatabase : RoomDatabase() {
    abstract val inputDao: TaskInputDao
    abstract val outputDao: TaskOutputDao
    abstract val extraDao: TaskExtraInputDao
}