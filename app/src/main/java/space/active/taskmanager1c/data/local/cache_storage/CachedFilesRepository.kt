package space.active.taskmanager1c.data.local.cache_storage

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.local.cache_storage.models.CachedFile
import space.active.taskmanager1c.data.remote.model.AuthBasicDto
import java.io.File

interface CachedFilesRepository {
    fun getFileList(auth: AuthBasicDto, cacheDirPath: String): Flow<List<CachedFile>>
    fun downloadFromServerToCache(
        auth: AuthBasicDto,
        cachedFile: CachedFile,
        cachePathName: String,
    ): Flow<Request<CachedFile>>

    fun getCurrentCachePath(cacheDirPath: String): File

    /**
     * Temp file uri from content resolver
     */
    fun getCacheUriForSave(cacheDirPath: String): Uri

    /**
     * save file to cache dir without uploading
     * return save result
     */
    fun saveExternalFileToCache(uri: Uri, cacheDirPath: String): Flow<Boolean>

    /**
     * file must include fileId and filename in the name at that format: "fileid@filename"
     */
    fun uploadFileToServer(
        auth: AuthBasicDto,
        cachedFile: CachedFile,
        cacheDirPath: String
    ): Flow<Request<CachedFile>>

    fun deleteCachedFile(cachedFile: CachedFile): Flow<Boolean>
    fun deleteFileFromServer(auth: AuthBasicDto, cachedFile: CachedFile, cacheDirPath: String)
}