package space.active.taskmanager1c.data.local.cache_storage

import android.app.Application
import android.os.FileObserver
import dagger.assisted.AssistedFactory
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
        if (event == CLOSE_WRITE || event == CREATE || event == DELETE) {
            setFileList()
        }
    }

    private fun setFileList() {
        val files = path.listFiles { file ->
            file.isFile
        }?.toList() ?: emptyList()
        flow.value = files
    }

    fun getFlow(): Flow<List<File>> = flow.asFlow()

    private fun <T> StateFlow<T>.asFlow(): Flow<T> = callbackFlow {
        collect { value ->
            trySend(value).isSuccess
        }
    }
}