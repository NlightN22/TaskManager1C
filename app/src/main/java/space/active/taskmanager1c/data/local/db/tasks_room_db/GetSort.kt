package space.active.taskmanager1c.data.local.db.tasks_room_db

object GetSort {
    operator fun invoke(sortType: SortType, sortField: SortField): Int {
        val classes: Map<Pair<SortType, SortField>, Int> = mapOf(
            (SortType.ASCENDING to SortField.NAME) to 1,
            (SortType.DESCENDING to SortField.NAME) to 2,
            (SortType.ASCENDING to SortField.DATE) to 3,
            (SortType.DESCENDING to SortField.DATE) to 4,
            (SortType.ASCENDING to SortField.END_DATE) to 5,
            (SortType.DESCENDING to SortField.END_DATE) to 6,
            (SortType.ASCENDING to SortField.PERFORMER) to 7,
            (SortType.DESCENDING to SortField.PERFORMER) to 8,
        )
        return classes[sortType to sortField] ?: 4 // DESCENDING DATE by default
    }

    fun getSortSQL(sortType: SortType): String {
        return when (sortType) {
            SortType.ASCENDING -> "ASC"
            SortType.DESCENDING -> "DESC"
        }
    }

    fun getFieldSQL(sortField: SortField): String {
        return when (sortField) {
            SortField.NAME -> "ORDER BY TaskInputHandled.name "
            SortField.DATE -> "ORDER BY TaskInputHandled.date "
            SortField.END_DATE -> "ORDER BY TaskInputHandled.endDate "
            SortField.PERFORMER -> "ORDER BY UserInput.userName "
        }
    }

    fun getFilterSQL(filterType: FilterType, filterExp: String): String {
        return when (filterType) {
            FilterType.ALl -> "SELECT * FROM TaskInputHandled " +
                    "LEFT JOIN UserInput " +
                    "ON UserInput.userId = TaskInputHandled.performerId "
            FilterType.IDo -> "SELECT * FROM TaskInputHandled " +
                    "LEFT JOIN UserInput " +
                    "ON UserInput.userId = TaskInputHandled.performerId " +
                    "LEFT JOIN CoPerformersInTask " +
                    "ON TaskInputHandled.id = CoPerformersInTask.taskId " +
                    "WHERE (CoPerformersInTask.coPerformerId = '$filterExp' " +
                    "OR TaskInputHandled.performerId = '$filterExp') " +
                    "AND (status = 'new' " +
                    "OR status = 'accepted' " +
                    "OR status = 'performed' " +
                    "OR status = 'deferred') "
            FilterType.IDelegate -> "SELECT * FROM TaskInputHandled " +
                    "LEFT JOIN UserInput " +
                    "ON UserInput.userId = TaskInputHandled.performerId " +
                    "WHERE (TaskInputHandled.authorId = '$filterExp' " +
                    "AND TaskInputHandled.performerId != '$filterExp') " +
                    "AND (status = 'new' " +
                    "OR status = 'accepted' " +
                    "OR status = 'performed' " +
                    "OR status = 'deferred') "
            FilterType.IDidNtCheck -> "SELECT * FROM TaskInputHandled " +
                    "LEFT JOIN UserInput " +
                    "ON UserInput.userId = TaskInputHandled.performerId " +
                    "WHERE TaskInputHandled.authorId = '$filterExp' " +
                    "AND status = 'reviewed' "
            FilterType.IObserve -> "SELECT * FROM TaskInputHandled " +
                    "LEFT JOIN UserInput " +
                    "ON UserInput.userId = TaskInputHandled.performerId " +
                    "INNER JOIN ObserversInTask " +
                    "ON TaskInputHandled.id = ObserversInTask.taskId " +
                    "AND ObserversInTask.observerId = '$filterExp' " +
                    "AND (status = 'new' " +
                    "OR status = 'accepted' " +
                    "OR status = 'performed' " +
                    "OR status = 'reviewed' " +
                    "OR status = 'deferred') "
        }
    }
}

enum class SortType {
    ASCENDING,
    DESCENDING
}

enum class SortField {
    NAME,
    DATE,
    END_DATE,
    PERFORMER
}

enum class FilterType {
    ALl,
    IDo,
    IDelegate,
    IDidNtCheck,
    IObserve
}
