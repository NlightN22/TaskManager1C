package space.active.taskmanager1c.data.local.cache_storage

import android.os.FileObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import space.active.taskmanager1c.coreutils.logger.Logger
import java.io.File
import javax.inject.Inject

private const val TAG = "TaskFilesObserver"

class TaskFilesObserver @Inject constructor(
    private val logger: Logger,
    private val path: File
) : FileObserver(path) {
    private val flow = MutableStateFlow<List<File>>(emptyList())

    init {
        startWatching()
        setFileList()
    }

    override fun onEvent(event: Int, path: String?) {
        if (event == DELETE || event == MODIFY || event == CREATE || event == MOVED_TO || event == MOVED_FROM ) {
            setFileList()
        }
    }

    private fun setFileList() {
        val files = path.listFiles { file ->
            file.isFile
        }?.toList()?.filterNot {
            it.name.contains(".part")
        } ?: emptyList()
        if (files != flow.value) {
            logger.log(TAG, "setFileList:\n${files.map { it.name }.joinToString("\n")}")
            flow.value = files
        }
    }

    fun getFlow(): Flow<List<File>> = flow.asFlow()

    private fun <T> StateFlow<T>.asFlow(): Flow<T> = callbackFlow {
        collect { value ->
            trySend(value).isSuccess
        }
    }
}