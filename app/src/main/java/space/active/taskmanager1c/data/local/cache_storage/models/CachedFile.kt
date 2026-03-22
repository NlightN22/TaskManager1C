package space.active.taskmanager1c.data.local.cache_storage.models

import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import java.io.File

data class CachedFile(
    val uri: Uri?,
    val id: String,
    val filename: String,
    val hash: String? = null,
    val sizeBytes: Long = 0L,
    val cached: Boolean = false,
    val loading: Boolean = false,
    val notUploaded: Boolean = false,
    val progress: Int = 0,
) {

    fun setCached(file: File) = this.copy(
        uri = file.toUri(),
        cached = true,
        sizeBytes = file.length()
    )

    fun toFile(): File = this.uri?.toFile() ?: throw IllegalStateException()

    companion object {
        fun List<CachedFile>.notContain(newList: List<CachedFile>): List<CachedFile> {
            val existingKeys = this.map { it.hash ?: "${it.id}|${it.filename}" }.toSet()
            return newList.filterNot { (it.hash ?: "${it.id}|${it.filename}") in existingKeys }
        }
    }
}