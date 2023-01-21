package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskInputHandledWithUsers

@Dao
interface SortedDao {

    @Transaction
    @Query(
        "SELECT * FROM TaskInputHandled " +
                " ORDER BY " +
                "CASE WHEN :isAsc = 1 THEN name END ASC, " +
                "CASE WHEN :isAsc = 2 THEN name END DESC, " +
                "CASE WHEN :isAsc = 3 THEN date END ASC, " +
                "CASE WHEN :isAsc = 4 THEN date END DESC, " +
                "CASE WHEN :isAsc = 5 THEN endDate END ASC, " +
                "CASE WHEN :isAsc = 6 THEN endDate END DESC "
    )
    fun getSortedTasks(isAsc: Int): Flow<List<TaskInputHandledWithUsers>>

    // not finished not cancelled not reviewed
    @Transaction
    @Query(
        "SELECT * " +
                "FROM TaskInputHandled " +
                "LEFT JOIN CoPerformersInTask " +
                "ON TaskInputHandled.id = CoPerformersInTask.taskId " +
                "AND (CoPerformersInTask.coPerformerId = :performerId " +
                "OR TaskInputHandled.performerId = :performerId)" +
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
    fun getTasksIDo(performerId: String, isAsc: Int): Flow<List<TaskInputHandledWithUsers>> // todo fix filtering

    // not finished not cancelled not reviewed
    @Transaction
    @Query(
        "SELECT * FROM TaskInputHandled " +
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
    fun getTasksIDelegate(authorId: String, isAsc: Int): Flow<List<TaskInputHandledWithUsers>>

    // reviewed
    @Transaction
    @Query(
        "SELECT * FROM TaskInputHandled " +
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
    fun getTasksIDidNtCheck(authorId: String, isAsc: Int): Flow<List<TaskInputHandledWithUsers>>

    // reviewed
    @Transaction
    @Query(
        "SELECT * FROM TaskInputHandled " +
                "INNER JOIN ObserversInTask " +
                "ON TaskInputHandled.id = ObserversInTask.taskId " +
                "AND ObserversInTask.observerId = :observerId " +
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
    fun getTasksIObserve(observerId: String, isAsc: Int): Flow<List<TaskInputHandledWithUsers>>

    // todo delete
    // reviewed
//    @Query("SELECT * FROM TaskInputHandled " +
//            "WHERE unread = 1 " +
//            "AND (status = 'new' " +
//            "OR status = 'accepted' " +
//            "OR status = 'performed' " +
//            "OR status = 'reviewed' " +
//            "OR status = 'deferred') " +
//            " ORDER BY " +
//            "CASE WHEN :isAsc = 1 THEN name END ASC, " +
//            "CASE WHEN :isAsc = 2 THEN name END DESC, " +
//            "CASE WHEN :isAsc = 3 THEN date END ASC, " +
//            "CASE WHEN :isAsc = 4 THEN date END DESC, " +
//            "CASE WHEN :isAsc = 5 THEN endDate END ASC, " +
//            "CASE WHEN :isAsc = 6 THEN endDate END DESC "
//    )
//    fun getTasksIDidNtRead(isAsc: Int): Flow<List<TaskInputHandledWithUsers>>

}