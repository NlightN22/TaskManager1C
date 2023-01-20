package space.active.taskmanager1c.domain.models

sealed class SaveEvents {
    data class Simple(
        val taskDomain: TaskDomain
    ) : SaveEvents()

    data class Delayed(
        val taskDomain: TaskDomain,
        val jobKey: String,
        val delay: Int
    ) : SaveEvents()

    data class Breakable(
        val taskDomain: TaskDomain,
        val cancelDuration: Int
    ) : SaveEvents()
    object BreakSave: SaveEvents()
}
