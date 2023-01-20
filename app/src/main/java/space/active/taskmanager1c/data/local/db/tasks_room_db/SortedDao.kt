package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInputHandled

@Dao
interface SortedDao {

    @Query("SELECT * FROM TaskInputHandled " +
            " ORDER BY " +
            "CASE WHEN :isAsc = 1 THEN name END ASC, " +
            "CASE WHEN :isAsc = 2 THEN name END DESC, " +
            "CASE WHEN :isAsc = 3 THEN date END ASC, " +
            "CASE WHEN :isAsc = 4 THEN date END DESC, " +
            "CASE WHEN :isAsc = 5 THEN endDate END ASC, " +
            "CASE WHEN :isAsc = 6 THEN endDate END DESC "
    )
    fun getSortedTasks(isAsc: Int): Flow<List<TaskInputHandled>>

    // not finished not cancelled not reviewed
    @Query("SELECT * FROM TaskInputHandled " +
            "WHERE performerId = :performerId " + // add join from coperformers
            "AND (status = 'new' " +
            "OR status = 'accepted' " +
            "OR status = 'performed' " +
            "OR status = 'deferred') " +
            " ORDER BY " +
            "CASE WHEN :isAsc = 1 THEN name END ASC, " +
            "CASE WHEN :isAsc = 2 THEN name END DESC, " +
            "CASE WHEN :isAsc = 3 THEN date END ASC, " +
            "CASE WHEN :isAsc = 4 THEN date END DESC, " +
            "CASE WHEN :isAsc = 5 THEN endDate END ASC, " +
            "CASE WHEN :isAsc = 6 THEN endDate END DESC "
    )
    fun getTasksIDo(performerId : String, isAsc: Int): Flow<List<TaskInputHandled>>

    // not finished not cancelled not reviewed
    @Query("SELECT * FROM TaskInputHandled " +
            "WHERE authorId = :authorId " +
            "AND (status = 'new' " +
            "OR status = 'accepted' " +
            "OR status = 'performed' " +
            "OR status = 'deferred') " +
            " ORDER BY " +
            "CASE WHEN :isAsc = 1 THEN name END ASC, " +
            "CASE WHEN :isAsc = 2 THEN name END DESC, " +
            "CASE WHEN :isAsc = 3 THEN date END ASC, " +
            "CASE WHEN :isAsc = 4 THEN date END DESC, " +
            "CASE WHEN :isAsc = 5 THEN endDate END ASC, " +
            "CASE WHEN :isAsc = 6 THEN endDate END DESC "
    )
    fun getTasksIDelegate(authorId : String, isAsc: Int): Flow<List<TaskInputHandled>>

    // reviewed
    @Query("SELECT * FROM TaskInputHandled " +
            "WHERE authorId = :authorId " +
            "AND status = 'reviewed' " +
            " ORDER BY " +
            "CASE WHEN :isAsc = 1 THEN name END ASC, " +
            "CASE WHEN :isAsc = 2 THEN name END DESC, " +
            "CASE WHEN :isAsc = 3 THEN date END ASC, " +
            "CASE WHEN :isAsc = 4 THEN date END DESC, " +
            "CASE WHEN :isAsc = 5 THEN endDate END ASC, " +
            "CASE WHEN :isAsc = 6 THEN endDate END DESC "
    )
    fun getTasksIDidNtCheck(authorId: String, isAsc: Int): Flow<List<TaskInputHandled>>

    // reviewed
    @Query("SELECT * FROM TaskInputHandled " +
            "WHERE authorId = :observerId " +  // todo create entity and replace authorId
            "AND (status = 'new' " +
            "OR status = 'accepted' " +
            "OR status = 'performed' " +
            "OR status = 'reviewed' " +
            "OR status = 'deferred') " +
            " ORDER BY " +
            "CASE WHEN :isAsc = 1 THEN name END ASC, " +
            "CASE WHEN :isAsc = 2 THEN name END DESC, " +
            "CASE WHEN :isAsc = 3 THEN date END ASC, " +
            "CASE WHEN :isAsc = 4 THEN date END DESC, " +
            "CASE WHEN :isAsc = 5 THEN endDate END ASC, " +
            "CASE WHEN :isAsc = 6 THEN endDate END DESC "
    )
    fun getTasksIObserve(observerId: String, isAsc: Int): Flow<List<TaskInputHandled>>

    // reviewed
    @Query("SELECT * FROM TaskInputHandled " +
            "WHERE unread = 1 " +
            "AND (status = 'new' " +
            "OR status = 'accepted' " +
            "OR status = 'performed' " +
            "OR status = 'reviewed' " +
            "OR status = 'deferred') " +
            " ORDER BY " +
            "CASE WHEN :isAsc = 1 THEN name END ASC, " +
            "CASE WHEN :isAsc = 2 THEN name END DESC, " +
            "CASE WHEN :isAsc = 3 THEN date END ASC, " +
            "CASE WHEN :isAsc = 4 THEN date END DESC, " +
            "CASE WHEN :isAsc = 5 THEN endDate END ASC, " +
            "CASE WHEN :isAsc = 6 THEN endDate END DESC "
    )
    fun getTasksIDidNtRead(isAsc: Int): Flow<List<TaskInputHandled>>

}