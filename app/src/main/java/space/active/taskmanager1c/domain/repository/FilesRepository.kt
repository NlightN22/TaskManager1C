package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.model.AuthBasicDto
import space.active.taskmanager1c.domain.models.InternalStorageFile
import java.io.File

interface FilesRepository {
    fun getFileList(auth: AuthBasicDto, taskId: String): Flow<List<InternalStorageFile>>
    fun downloadFileToCache(auth: AuthBasicDto, taskId: String, fileId: String, fileName: String): Flow<Request<Any>>
    fun uploadFileToServer(file: File): Flow<Request<Any>>
}