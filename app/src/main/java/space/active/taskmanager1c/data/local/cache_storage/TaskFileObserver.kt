package space.active.taskmanager1c.data.local.cache_storage

import android.os.FileObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import space.active.taskmanager1c.coreutils.logger.Logger
import java.io.File
import javax.inject.Inject

private const val TAG = "TaskFilesObserver"

class TaskFilesObserver @Inject constructor(
    private val logger: Logger) {

    fun observe(inPath: String): Flow<List<File>> = channelFlow {
        val listener =
            object : FileObserver(File(inPath), CREATE or DELETE or MOVE_SELF or MOVED_FROM or MOVED_TO) {
                override fun onEvent(event: Int, path: String?) {
                    if (isActive) {
                        path?.let {
                            trySend(setFileList(inPath))
                        }
                    }
                }

                override fun startWatching() {
                    logger.log(TAG, "startWatching: $this")
                    super.startWatching()
                }

                override fun stopWatching() {
                    logger.log(TAG, "stopWatching: $this")
                    super.stopWatching()
                }
            }
        listener.startWatching()
        send(setFileList(inPath))

        awaitClose {
            listener.stopWatching()
        }
    }

    private fun setFileList(path: String): List<File> {
        val cacheDir = path.toFile()
        return cacheDir.listFiles { file ->
            file.isFile
        }?.toList()?.filterNot {
            it.name.contains(".part")
        } ?: emptyList()
    }

    private fun String.toFile(): File {
        val tryFile = File(this)
        if (tryFile.exists()) {
            return tryFile
        } else {
            throw IllegalStateException()
        }
    }
}