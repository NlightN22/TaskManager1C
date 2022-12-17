package space.active.taskmanager1c.domain.models

sealed class SaveEvents {
    data class Simple(
        val task: Task
    ) : SaveEvents()

    data class Delayed(
        val task: Task,
        val jobKey: String,
        val delay: Int
    ) : SaveEvents()

    data class Breakable(
        val task: Task,
        val cancelDuration: Int
    ) : SaveEvents()
    object BreakSave: SaveEvents()
}
