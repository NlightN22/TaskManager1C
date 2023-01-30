package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.UserSettings

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: UserSettings)

    @Query("SELECT * FROM UserSettings")
    suspend fun getSettings(): UserSettings?

    @Query("DELETE FROM UserSettings")
    suspend fun deleteAll()

    @Query("UPDATE UserSettings SET skipStatusAlert = :status WHERE id = 1")
    suspend fun updateStatusAlert(status: Boolean)

    @Query("SELECT UserSettings.skipStatusAlert FROM UserSettings WHERE id = 1")
    suspend fun getStatusAlert(): Boolean

}