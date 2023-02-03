package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.*
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
        UserSettings::class,
        ReadingTimesTaskEntity::class
    ],
    version = 4,
    )
@TypeConverters(Converters::class)
abstract class TaskWithUsersDatabase : RoomDatabase() {
    abstract val inputDao: TaskInputDao
    abstract val outputDao: TaskOutputDao
    abstract val settingsDao: SettingsDao
    abstract val readingDao: TaskReadingDao
}