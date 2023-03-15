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
    val notUploaded: Boolean = false,
    val progress: Int = 0,
) {


//    override fun equals(other: Any?): Boolean {
//        return other is CachedFile
//                && this.id == other.id
//                && this.uri == other.uri
//                && this.filename == other.filename
//                && this.cached == other.cached
//                && this.loading == this.loading
//                && this.notUploaded == this.notUploaded
//    }
//
//    override fun hashCode(): Int {
//        var hash = super.hashCode()
//        hash = 89 * hash + (this.id.hashCode() ?: 0)
//        hash = 89 * hash + (this.uri?.hashCode() ?: 0)
//        hash = 89 * hash + (this.filename.hashCode() ?: 0)
//        hash = 89 * hash + (this.cached.hashCode() ?: 0)
//        hash = 89 * hash + (this.loading.hashCode() ?: 0)
//        hash = 89 * hash + (this.notUploaded.hashCode() ?: 0)
//        return hash
//    }

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