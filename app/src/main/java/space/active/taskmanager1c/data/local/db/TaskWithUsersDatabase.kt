package space.active.taskmanager1c.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import space.active.taskmanager1c.data.local.db.entity.MessageDb
import space.active.taskmanager1c.data.local.db.entity.TaskDb
import space.active.taskmanager1c.data.local.db.entity.UserDb


@Database(
    entities = [TaskDb::class, UserDb::class, MessageDb::class],
    version = 1,

)
@TypeConverters(Converters::class)
abstract class TaskWithUsersDatabase: RoomDatabase() {
    abstract val dao: TaskWithUsersDao
}