package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.CoPerformersInTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.ObserversInTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInputHandled
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.Label
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.UserSettings
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask


@Database(
    entities = [
        TaskInputHandled::class,
        CoPerformersInTask::class,
        ObserversInTask::class,
        UserInput::class,
        OutputTask::class,
        Label::class,
        UserSettings::class
    ],
    version = 1,
    )
@TypeConverters(Converters::class)
abstract class TaskWithUsersDatabase : RoomDatabase() {
    abstract val inputDao: TaskInputDao
    abstract val outputDao: TaskOutputDao
    abstract val settingsDao: SettingsDao
}