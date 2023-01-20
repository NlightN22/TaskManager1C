package space.active.taskmanager1c.data.local.db.tasks_room_db

object GetSortInt {
    operator fun invoke(sortType: SortType, sortField: SortField): Int {
        val classes: Map<Pair<SortType, SortField>, Int> = mapOf(
            (SortType.ASCENDING to SortField.NAME) to 1,
            (SortType.DESCENDING to SortField.NAME) to 2,
            (SortType.ASCENDING to SortField.DATE) to 3,
            (SortType.DESCENDING to SortField.DATE) to 4,
            (SortType.ASCENDING to SortField.END_DATE) to 5,
            (SortType.DESCENDING to SortField.END_DATE) to 6,
        )
        return classes[sortType to sortField] ?: 4 // DESCENDING DATE by default
    }
}

enum class SortType {
    ASCENDING,
    DESCENDING
}

enum class SortField {
    NAME,
    DATE,
    END_DATE
}
