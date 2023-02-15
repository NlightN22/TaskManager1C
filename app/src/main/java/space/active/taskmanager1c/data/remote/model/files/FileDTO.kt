package space.active.taskmanager1c.data.remote.model.files

import androidx.core.net.toUri
import space.active.taskmanager1c.data.local.cache_storage.models.CachedFile
import java.io.File

data class FileDTO(
    val fileID: String,
    val fileName: String
) {
    fun toNotCachedFile() = CachedFile(
        uri = null,
        id = fileID,
        filename = fileName
    )
    fun toCachedFile(file: File) = CachedFile(
        uri = file.toUri(),
        id = fileID,
        filename = fileName,
        cached = true,
        notUploaded = false
    )
}