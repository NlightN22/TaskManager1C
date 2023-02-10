package space.active.taskmanager1c.data.remote.model.files

import space.active.taskmanager1c.domain.models.InternalStorageFile

data class FileDTO(
    val fileID: String,
    val fileName: String
) {
    fun toInternalStorageFile() = InternalStorageFile(
        uri = null,
        id = fileID,
        filename = fileName
    )
}