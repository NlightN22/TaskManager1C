package space.active.taskmanager1c.coreutils.logger

interface Logger {
    fun log(localTag: String, message: String)
    fun error(localTag: String, message: String)
    companion object{
        const val MAIN_TAG = "TaskManager1C"
    }
}