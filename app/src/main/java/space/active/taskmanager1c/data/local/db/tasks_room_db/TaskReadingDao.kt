package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.ReadingTimesTaskEntity

@Dao
interface TaskReadingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(readingTimesTaskEntity: ReadingTimesTaskEntity)

    @Query("SELECT ReadingTimesTaskEntity.* " +
            "FROM ReadingTimesTaskEntity " +
            "WHERE mainTaskId = :mainTaskId")
    suspend fun getReading(mainTaskId: String): ReadingTimesTaskEntity?

    @Query("SELECT ReadingTimesTaskEntity.mainTaskId " +
            "FROM ReadingTimesTaskEntity " +
            "WHERE ReadingTimesTaskEntity.isUnread = 1")
    fun getUnreadIds(): Flow<List<String>>
}