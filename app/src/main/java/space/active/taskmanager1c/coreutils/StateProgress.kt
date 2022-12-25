package space.active.taskmanager1c.coreutils

sealed class StateProgress<T>

class Loading<T> : StateProgress<T>()
class OnWait<T> : StateProgress<T>()
data class Success<T>(
    val data: T
) : StateProgress<T>()
