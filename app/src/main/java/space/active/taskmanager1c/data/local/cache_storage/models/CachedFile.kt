package space.active.taskmanager1c.data.local.cache_storage.models

import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import java.io.File

data class CachedFile(
    val uri: Uri?,
    val id: String,
    val filename: String,
    val cached: Boolean = false,
    val loading: Boolean = false,
    val notUploaded: Boolean = false
) {
    fun setLoading(state: Boolean) = this.copy(
        loading = state
    )

    fun setUploaded(state: Boolean) = this.copy(
        notUploaded = false
    )

    fun setCached(file: File) = this.copy(
        uri = file.toUri(),
        cached = true
    )

    fun toFile(): File  = this.uri?.toFile() ?: throw IllegalStateException()

    companion object {
        fun List<CachedFile>.notContain(newList: List<CachedFile>): List<CachedFile> {
            return newList.filterNot { this.contains(it) }
        }
    }
}